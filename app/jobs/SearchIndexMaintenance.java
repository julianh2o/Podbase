// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
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
import play.modules.search.store.FilesystemStore;
import play.test.*;
import services.PathService;
import services.PermissionService;
import util.TaskCompletionEstimator;
 
import models.*;
 
//@Every("1h")
public class SearchIndexMaintenance extends Job {
    public void doJob() throws Exception {
    	System.out.println("Rebuilding all indexes..");
    	Search.rebuildAllIndexes();
    	System.out.println("Indexes rebuilt!");
    }
    
    public void doJobOld() throws Exception {
    	try {
	    	long start = System.currentTimeMillis();
	    	
	    	System.out.println("Loading objects..");
	    	List<ImageAttribute> imageAttributes = ImageAttribute.findAll();
	    	System.out.println("Objects loaded: "+imageAttributes.size());
	    	System.out.println("Indexing..");
	    	TaskCompletionEstimator est = new TaskCompletionEstimator(10,10000,imageAttributes.size());
			((FilesystemStore)Search.getCurrentStore()).sync = false;
			
			long lastLog = System.currentTimeMillis();
	    	for (ImageAttribute attr : imageAttributes) {
		        Search.index(attr);
		        est.tick();
		        
		        if (System.currentTimeMillis() - lastLog > 10000) {
					lastLog = System.currentTimeMillis();
		        	System.out.println(est.getStatusLine());
		        }
	    	}
			((FilesystemStore)Search.getCurrentStore()).sync = true;
	    	
	        long duration = System.currentTimeMillis() - start;
	        long seconds = (int)(duration / 1000);
	        System.out.println("Index rebuilt in "+seconds+" seconds.");
    	} catch (Exception e) {
    		System.out.println("Rebuilding index failed!");
    		e.printStackTrace();
    	}
    }
}
