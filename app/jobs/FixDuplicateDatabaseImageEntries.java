package jobs;

import java.util.LinkedList;
import java.util.List;

import models.DatabaseImage;
import models.ImageAttribute;
import play.jobs.Job;

public class FixDuplicateDatabaseImageEntries extends ManagedJob {
	public void doJob() throws Exception {
		System.out.println("finding duplicate images");
    	List<DatabaseImage> images = DatabaseImage.find("SELECT img FROM DatabaseImage img WHERE (SELECT COUNT(imgs) FROM DatabaseImage imgs WHERE imgs.path=img.path) > 1").fetch();
    	System.out.println(images.size()+" paths with duplicates found");
    	
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
	}
}
