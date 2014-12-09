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
import java.util.Set;

import org.apache.commons.io.FileUtils;

import models.DatabaseImage;
import models.ImageAttribute;
import models.ProjectVisibleImage;
import play.jobs.Job;
import services.PathService;
import util.Stopwatch;
import util.StringUtil;
import util.TaskCompletionEstimator;

public class FindMissingFileMatches extends ManagedJob {
	public void doJob() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024*1024;
        
        File f = PathService.prepareOutputFile("missingFilesWithAttributes.txt");
        List<String> lines = FileUtils.readLines(f);
    	
    	TaskCompletionEstimator est = new TaskCompletionEstimator(10,10,lines.size());
		StringBuilder byHash = new StringBuilder();
		StringBuilder byName = new StringBuilder();
		StringBuilder noMatch = new StringBuilder();
    	for (String line : lines) {
    		String[] split = line.split("\t");
    		String imgRelPath = split[1];
    		DatabaseImage img = DatabaseImage.forPath(PathService.resolve(imgRelPath));
    		if (img == null) {
    			noMatch.append("null image for: "+imgRelPath+"\n");
    			continue;
    		}
    		List<DatabaseImage> withMatchingName = DatabaseImage.find("path like ?", "%"+img.getPath().getFileName().toString()).fetch();
    		List<DatabaseImage> withMatchingHash = DatabaseImage.find("byHash", img.hash).fetch();
    		if (withMatchingHash.size() > 1) {
    			List<HashMap<String,String>> values = new LinkedList<>();
    			for (DatabaseImage image : withMatchingHash) {
    				byHash.append((img == image ? "*" : " ")+image.attributes.size()+((image.attributes.size() > 9) ? " " : "  ")+image.getStringPath()+"\n");
    				HashMap<String,String> map = new HashMap<>();
    				for (ImageAttribute attr : image.attributes) {
    					map.put(attr.attribute, attr.value);
    				}
    				values.add(map);
    			}
    			Set<String> keys = new HashSet<>();
    			for (HashMap<String,String> map : values) keys.addAll(map.keySet());
    			
    			for (String key : keys) {
    				String firstVal = values.get(0).get(key);
    				boolean identical = true;
    				for (HashMap<String,String> map : values) {
    					if ((firstVal == null && map.get(key) != null)
    							|| (firstVal != null && map.get(key) == null)
    							|| (map.get(key) != null && !map.get(key).equals(firstVal))) {
    						identical = false;
    						break;
    					}
    				}
    				byHash.append(
	    				"   "+StringUtil.padRight((identical ? "" : "* ")+key, 30)
	    			);
    				for (HashMap<String,String> map : values) {
    					int padTo = 40;
    					String val = map.get(key);
    					if (val == null) val = "[NO VALUE]";
    					if (val.length() > padTo-4) val = val.substring(0, padTo-4)+"...";
    					byHash.append(StringUtil.padRight(val, padTo));
    				}
    				byHash.append("\n");
    			}
    			byHash.append("\n");
    		} else if (withMatchingName.size() > 1){
    			for (DatabaseImage image : withMatchingHash) {
    				byName.append((img == image ? "*" : " ")+image.attributes.size()+((image.attributes.size() > 9) ? " " : "  ")+image.getStringPath()+"\n");
    			}
    			byName.append("\n");
    		} else {
    			noMatch.append("Not matched: "+imgRelPath+"\n");
    		}
    		est.tick(true);
    	}
    	
    	FileUtils.writeStringToFile(PathService.prepareOutputFile("matchedFiles.txt"), "Matched by hash: \n"+byHash.toString()+"\n\n\nMatched by name\n"+byName.toString()+"\n\n\nNot Matched:\n"+noMatch.toString());
	}
}
