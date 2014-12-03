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
import util.Stopwatch;
import util.TaskCompletionEstimator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import access.Access;
import access.AccessType;
import access.ModelAccess;

import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
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
    
    public static void fixDuplicateImageEntries() {
    	User user = Security.getUser();
    	if (!user.isRoot()) forbidden();
    	List<DatabaseImage> images = DatabaseImage.find("SELECT img FROM DatabaseImage img WHERE (SELECT COUNT(imgs) FROM DatabaseImage imgs WHERE imgs.path=img.path) > 1").fetch();
    	for (DatabaseImage img : images) {
    		List<DatabaseImage> duplicates = DatabaseImage.find("byPath", img.getStringPath()).fetch();
    		List<ImageAttribute> attributes = new LinkedList<ImageAttribute>();
    		DatabaseImage first = null;
    		for (DatabaseImage dup : duplicates) {
    			if (first == null) {
    				first = dup;
    			} else {
	    			for (ImageAttribute attr : dup.attributes) {
	    				attributes.add(attr);
	    				attr.image = dup;
	    				attr.save();
	    			}
	    			dup.delete();
    			}
    		}
    	}
    	renderText(images.size()+" images with duplicates cleaned up.");
    }
    
    public static void fixMissingFiles() throws IOException {
    	User user = Security.getUser();
    	if (!user.isRoot()) forbidden();
        Runtime runtime = Runtime.getRuntime();
        
        int mb = 1024*1024;

    	System.out.println("loading images...");
    	List<DatabaseImage> images = DatabaseImage.all().fetch();
    	System.out.println("loaded "+images.size()+" images.");
    	
    	System.out.println("loading pvis");
    	List<ProjectVisibleImage> pvis = ProjectVisibleImage.all().fetch();
    	System.out.println("loaded "+pvis.size()+" pvis");
    	
    	HashMap<Long,ProjectVisibleImage> byImageId = new HashMap<>();
    	for (ProjectVisibleImage pvi : pvis) {
    		byImageId.put(pvi.image.id, pvi);
    	}
    	System.out.println("finished processing pvis");
    	
    	int deleted = 0;
    	int directories = 0;
    	StringBuilder sb = new StringBuilder();
    	TaskCompletionEstimator est = new TaskCompletionEstimator(10,10,images.size());
    	System.out.println("loading paths..");
    	HashSet<String> existingPaths = getAllFiles(PathService.getRootImageDirectory());
    	System.out.println("found "+existingPaths.size()+" files");
		long lastLog = System.currentTimeMillis();
		Stopwatch sw = new Stopwatch();
		List<Long> pviDeleteList = new LinkedList<>();
		List<Long> imageDeleteList = new LinkedList<>();
    	for (DatabaseImage img : images) {
    		sw.start("all");
    		est.tick();
    		boolean pathExists = existingPaths.contains(img.getStringPath());
    		if (!pathExists) {
    			boolean isDirectory = img.getPath().toFile().isDirectory();
    			if (isDirectory) {
    				directories++;
    			} else if (img.attributes.size() == 0) {
    				if (byImageId.containsKey(img.id)) {
    					pviDeleteList.add(byImageId.get(img.id).id);
    				}
					imageDeleteList.add(img.id);
	    			deleted++;
	    		} else {
	    			sb.append(img.getStringPath()+" does not exist, but has "+img.attributes.size()+" attributes.\n");
	    		}
	    		
		        if (System.currentTimeMillis() - lastLog > 10000) {
					lastLog = System.currentTimeMillis();
		        	System.out.println(est.getStatusLine());
			        System.out.println("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / mb) + " of "+(runtime.totalMemory()/mb)+" MB");
		        	//System.out.println(sw.toString());
		        }
    		}
    		sw.stop("all");
    	}
    	
    	StringBuilder sql = new StringBuilder();
    	int index = 0;
    	for (Long id : pviDeleteList) {
    		if (index == 0) 
    			sql.append("DELETE FROM ProjectVisibleImage WHERE id IN (");
    		else
    			sql.append(",");
    		sql.append(id);
    		index++;
    		if (index % 100 == 0) {
    			sql.append(");\n");
    			index = 0;
    		}
    	}
    	if (index != 0) sql.append(");\n");
    	
    	for (Long id : imageDeleteList) {
    		if (index == 0) 
    			sql.append("DELETE FROM DatabaseImage WHERE id IN (");
    		else
    			sql.append(",");
    		sql.append(id);
    		index++;
    		if (index % 100 == 0) {
    			sql.append(");\n");
    			index = 0;
    		}
    	}
    	if (index != 0) sql.append(");\n");
    	
    	File f = PathService.getApplicationPath().resolve("fixMissingFiles.sql").toFile();
    	System.out.println("writing to: "+f.getAbsolutePath());
    	FileUtils.writeStringToFile(f, sql.toString());
    	
    	renderText(sb.toString()+"\nWriting to fixMissingFiles.sql\n"+deleted+" nonexistent and un-attributed images deleted. "+directories+" directories were not deleted.");
    }
    
    private static HashSet<String> getAllFiles(Path root) throws IOException {
    	final HashSet<String> paths = new HashSet<>();
    	Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
		    @Override
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		    	paths.add(PathService.getRelativeString(file));
		        return FileVisitResult.CONTINUE;
		    }
    	});
		return paths;
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
