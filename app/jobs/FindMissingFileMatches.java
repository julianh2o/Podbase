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

public class FindMissingFileMatches extends ManagedJob {
	public void doJob() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024*1024;
        
        File f = PathService.prepareOutputFile("missingFilesWithAttributes.txt");
        List<String> lines = FileUtils.readLines(f);
    	
    	TaskCompletionEstimator est = new TaskCompletionEstimator(10,10,lines.size());
    	int bothUnique = 0;
    	int uniqueName = 0;
    	int uniqueHash = 0;
    	for (String line : lines) {
    		String[] split = line.split("\t");
    		String imgRelPath = split[1];
    		DatabaseImage img = DatabaseImage.forPath(PathService.resolve(imgRelPath));
    		List<DatabaseImage> withMatchingName = DatabaseImage.find("path like ?", "%"+img.getPath().getFileName().toString()).fetch();
    		List<DatabaseImage> withMatchingHash = DatabaseImage.find("byHash", img.hash).fetch();
    		if (withMatchingName.size() == 1 && withMatchingHash.size() == 1) {
    			bothUnique++;
    		} else if (withMatchingName.size() == 1) {
    			uniqueName++;
    		} else if (withMatchingHash.size() == 1) {
    			uniqueHash++;
    		}
    		//System.out.println(withMatchingHash.size()+" "+withMatchingName.size()+" "+imgRelPath);
    		est.tick(true);
    	}
    	System.out.println("Both Unique: "+bothUnique);
    	System.out.println("Unique Hash: "+uniqueHash);
    	System.out.println("Unique Name: "+uniqueName);
	}
}
