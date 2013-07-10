package jobs;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import controllers.ImageBrowser;

import access.AccessType;

import play.*;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.jobs.*;
import play.modules.search.Search;
import play.test.*;
import services.PathService;
import services.PermissionService;
import util.TaskCompletionEstimator;
 
import models.*;
 
//@Every("1h")
public class SearchIndexMaintenance extends Job {
    public void doJob() throws Exception {
    	try {
	    	long start = System.currentTimeMillis();
	    	
	    	System.out.println("Loading objects..");
	    	List<ImageAttribute> imageAttributes = ImageAttribute.findAll();
	    	System.out.println("Objects loaded: "+imageAttributes.size());
	    	System.out.println("Indexing..");
	    	TaskCompletionEstimator est = new TaskCompletionEstimator(10,20,imageAttributes.size());
	    	for (ImageAttribute attr : imageAttributes) {
		        Search.index(attr);
		        est.tick();
		        
		        if (est.getCurrentTick() % 300 == 0) {
		        	System.out.println(est.getStatusLine());
		        }
	    	}
	    	
	        long duration = System.currentTimeMillis() - start;
	        long seconds = (int)(duration / 1000);
	        System.out.println("Index rebuilt in "+seconds+" seconds.");
    	} catch (Exception e) {
    		System.out.println("Rebuilding index failed!");
    		e.printStackTrace();
    	}
    }
}
