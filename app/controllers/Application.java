// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import play.*;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.modules.search.Search;
import play.mvc.*;
import services.PathService;

import java.text.DecimalFormat;
import java.util.*;
import access.AccessType;
import access.ModelAccess;

import javax.persistence.Query;

import jobs.SearchIndexMaintenance;

import models.*;

@With(Security.class)
public class Application extends ParentController {

    public static void index() {
    	render();
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
	
	public static void rebuildIndex() throws Exception {
		new SearchIndexMaintenance().now();
	}
	
	public static void testInsert() {
		Project p = Project.find("byName", "TestProject").first();
		DatabaseImage dbi = DatabaseImage.forPath(PathService.resolve("/TestProject/CW080113_Acartia1_10x_BF.jpg"));
		
		long time = System.currentTimeMillis();
		dbi.addAttribute(p, "test key", "value", true);
		System.out.println("Insert took: "+(System.currentTimeMillis() - time)+"ms");
	}
}
