package jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.DatabaseImage;
import models.ImageAttribute;
import models.ProjectVisibleImage;
import play.jobs.Job;
import services.PathService;
import util.Stopwatch;
import util.TaskCompletionEstimator;

public class FixMissingFiles extends ManagedJob {
	public void doJob() throws Exception {
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
    	StringBuilder fileNotFoundMessages = new StringBuilder();
    	TaskCompletionEstimator est = new TaskCompletionEstimator(10,10,images.size());
    	System.out.println("loading paths..");
    	HashSet<String> existingPaths = getAllFiles(PathService.getRootImageDirectory());
    	System.out.println("found "+existingPaths.size()+" files");
		long lastLog = System.currentTimeMillis();
		Stopwatch sw = new Stopwatch();
		List<Long> pviDeleteList = new LinkedList<>();
		List<Long> imageDeleteList = new LinkedList<>();
		int found = 0;
    	for (DatabaseImage img : images) {
    		sw.start("all");
    		est.tick();
    		boolean pathExists = existingPaths.contains(img.getStringPath());
    		if (!pathExists) {
    			found ++;
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
	    			fileNotFoundMessages.append(img.attributes.size()+"\t"+img.getStringPath()+"\n");
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
    	
    	System.out.println("found: "+found+" of "+images.size());
    	
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
    	
    	index = 0;
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
    	
    	System.out.println("writing to: "+PathService.prepareOutputFile("fixMissingFiles.sql").getAbsolutePath());
    	FileUtils.writeStringToFile(PathService.prepareOutputFile("fixMissingFiles.sql"), sql.toString());
    	
    	System.out.println("writing to: "+PathService.prepareOutputFile("missingFilesWithAttributes.txt").getAbsolutePath());
    	FileUtils.writeStringToFile(PathService.prepareOutputFile("missingFilesWithAttributes.txt"), fileNotFoundMessages.toString());
    	
    	System.out.println(deleted+" nonexistent and un-attributed images deleted. "+directories+" directories were not deleted.");
	}
	
    private static HashSet<String> getAllFiles(Path root) throws IOException {
    	final HashSet<String> paths = new HashSet<>();
    	Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
		    @Override
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		    	System.out.println(PathService.getRelativeString(file));
		    	paths.add(PathService.getRelativeString(file));
		        return FileVisitResult.CONTINUE;
		    }
    	});
		return paths;
	}

}
