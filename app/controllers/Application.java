// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import play.*;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.modules.search.Search;
import play.modules.search.store.FilesystemStore;
import play.mvc.*;
import services.PathService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import access.Access;
import access.AccessType;
import access.ModelAccess;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.ivy.util.FileUtil;

import jobs.SearchIndexMaintenance;
import models.*;

@With(Security.class)
public class Application extends ParentController {
    public static void index() {
    	render();
    }
    
    public static void heartbeat() {
    	User user = Security.getUser();
    	user.lastActive = new Date();
    	user.save();
    }
    
    public static void redirectTo(String url) {
    	redirect(url);
    }
    
    public static void updateDocs() throws IOException, InterruptedException {
    	User user = Security.getUser();
    	if (!user.isRoot()) forbidden();
    	File tmpFolder = Play.applicationPath.toPath().resolve("./tmp").toFile();
    	File docsFolder = Play.applicationPath.toPath().resolve("./tmp/Podbase_docs").toFile();
    	String gitPath = Play.configuration.getProperty("podbase.gitpath",null);
    	if (gitPath == null) throw new RuntimeException("Git path not set! Configure podbase.gitpath to use this feature");
    	if (docsFolder.exists()) {
	        ProcessBuilder pb = new ProcessBuilder(gitPath,"pull","origin","master");
	        pb.directory(docsFolder);
	        Process p = pb.start();
    	} else {
	        ProcessBuilder pb = new ProcessBuilder(gitPath,"clone","https://github.com/julianh2o/Podbase_docs");
	        pb.directory(tmpFolder);
	        Process p = pb.start();
    	}
    	jsonOk();
    }
    
    @ModelAccess(AccessType.LISTED)
    public static void entry(Long projectId) {
    	Project project = Project.findById(projectId);
    	if (project == null) error("Project not found!");
    	render(project);
    }
    
    public static void paper(Long paperId) {
    	Paper paper = Paper.findById(paperId);
    	if (paper == null) index();
    	render(paper);
    }
    
	public static void loadJavascript(String path) {
		renderTemplate("/public/javascripts/"+path);
	}
	
	public static void getCurrentUser() {
		User user = Security.getUser();
		renderJSON(user);
	}
	
//	public static void rebuildIndex() throws Exception {
//		new SearchIndexMaintenance().now();
//	}
	
//	public static void indexImage(Path path) throws Exception {
//		((FilesystemStore)Search.getCurrentStore()).sync = true;
//		DatabaseImage dbi = DatabaseImage.forPath(path);
//		for (ImageAttribute attr : dbi.attributes) {
//			System.out.println("indexing.. "+attr.value);
//			Search.index(attr);
//		}
//	}
	
//	public static void testInsert() {
//		Project p = Project.find("byName", "TestProject").first();
//		DatabaseImage dbi = DatabaseImage.forPath(PathService.resolve("/TestProject/CW080113_Acartia1_10x_BF.jpg"));
//		
//		long time = System.currentTimeMillis();
//		dbi.addAttribute(p, "test key", "value", true);
//		System.out.println("Insert took: "+(System.currentTimeMillis() - time)+"ms");
//	}
	
	public static void findOrphanedData() {
		List<DatabaseImage> all = DatabaseImage.findAll();
		StringBuffer sb = new StringBuffer();
		
		Set<Path> duplicateDetection = new HashSet<Path>();
		for (DatabaseImage image : all) {
			if (!PathService.isValidPath(image.getStringPath())) {
				sb.append("Invalid Path: "+image.getStringPath()+"\n");
				continue;
			}
			
			Path path = image.getPath();
			if (duplicateDetection.contains(path)) {
				sb.append("Duplicate Path: "+image.getStringPath()+"\n");
			}
			duplicateDetection.add(path);
			File f = path.toFile();
			if (!f.exists()) {
				sb.append("Missing: "+image.getStringPath()+"\n");
				continue;
			}
			
			if (f.isDirectory()) sb.append("Directory: "+image.getStringPath()+"\n");
		}
		
		renderText(sb.toString());
	}
	
	@Access(AccessType.CREATE_PAPER)
	public static void fixOrphanedData() throws IOException {
		System.out.println("Fixing Orphaned Data...");
		List<DatabaseImage> all = DatabaseImage.findAll();
		System.out.println("Found "+all.size()+" total images.");
		
		HashMap<Path,List<DatabaseImage>> imagesByPath = new HashMap<Path,List<DatabaseImage>>();
		
		List<DatabaseImage> imageDeleteList = new LinkedList<DatabaseImage>();
		
		for (DatabaseImage image : all) {
			if (!PathService.isValidPath(image.getStringPath())) {
				imageDeleteList.add(image);
				continue;
			}
			
			Path path = image.getPath();
			if (!imagesByPath.containsKey(path)) imagesByPath.put(path, new LinkedList<DatabaseImage>());
			imagesByPath.get(path).add(image);
			File f = path.toFile();
			if (!f.exists()) {
				//imageDeleteList.add(image);
				continue;
			}
			
			if (f.isDirectory()) {
				imageDeleteList.add(image);
			}
		}
		System.out.println("Initial triage complete.");
		
		StringBuffer moveAttributes = new StringBuffer();
		List<DatabaseImage> imageDuplicateDeleteList = new LinkedList<DatabaseImage>();
		for (Entry<Path,List<DatabaseImage>> entry : imagesByPath.entrySet()) {
			List<DatabaseImage> images = entry.getValue();
			if (images.size() > 1) {
				List<ImageAttribute> attributes = new LinkedList<ImageAttribute>();
				for(DatabaseImage image : images) {
					if (image != images.get(0)) {
						//if (image.attributes.size() > 0) System.out.println(image.attributes.size());
						for (ImageAttribute attr : image.attributes) {
							attributes.add(attr);
						}
						
						imageDuplicateDeleteList.add(image);
					}
				}
				
				if (attributes.size() > 0) {
					StringBuffer attrIds = new StringBuffer();
					for (ImageAttribute attr : attributes) {
						attrIds.append(","+attr.id);
					}
					moveAttributes.append(String.format("UPDATE ImageAttribute SET image_id=%d WHERE id IN (%s);\n",images.get(0).id,attrIds.substring(1)));
				}
			}
		}
		System.out.println("Attribute reassignment complete.");
		
		if (imageDeleteList.size() == 0 && imageDuplicateDeleteList.size() == 0) {
			renderText("No Images to delete!");
			return;
		}
		
		StringBuffer imageList = new StringBuffer();
		StringBuffer attributeList = new StringBuffer();
		for (DatabaseImage image : imageDeleteList) {
			imageList.append(","+image.id);
			for (ImageAttribute attr : image.attributes) {
				if (attr.linkedAttribute != null) {
					attr.linkedAttribute.linkedAttribute = null;
					attr.linkedAttribute.save();
					attr.linkedAttribute = null;
					attr.save();
				}
				attributeList.append(","+attr.id);
			}
		}
		
		for (DatabaseImage image : imageDuplicateDeleteList) {
			imageList.append(","+image.id);
		}
		
		System.out.println("Writing deleteImages.sql");
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./deleteImages.sql")));
		
		String projectVisibleImageSql = String.format("DELETE FROM ProjectVisibleImage WHERE image_id IN (%s);",imageList.toString().substring(1));
		String imageSetSql = String.format("DELETE FROM ImageSetMembership WHERE image_id IN (%s);",imageList.toString().substring(1));
		String imageSql = String.format("DELETE FROM DatabaseImage WHERE id IN (%s);",imageList.toString().substring(1));
		
		bw.write(projectVisibleImageSql);
		bw.write("\n");
		bw.write(imageSetSql);
		bw.write("\n");
		
		bw.write(moveAttributes.toString());
		bw.write("\n");
		
		if (attributeList.length() != 0) {
			String attributeSql = String.format("DELETE FROM ImageAttribute WHERE id IN (%s);",attributeList.toString().substring(1));
			bw.write(attributeSql);
			bw.write("\n");
		}
		
		bw.write(imageSql);
		bw.write("\n");
		bw.close();
		
		System.out.println("Fixing Orphans Complete, execute deleteImages.sql");
		renderText(String.format("Marked %d images for deletion.",imageDeleteList.size()));
	}
}
