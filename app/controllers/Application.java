package controllers;

import play.*;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.modules.search.Search;
import play.mvc.*;

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
    
    @ModelAccess(AccessType.VISIBLE)
    public static void entry(Long projectId) {
    	Project project = Project.findById(projectId);
    	if (project == null) index();
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
	
//	public static void testSearch(String query) {
//		List<ImageAttribute> found = Search.search(query, ImageAttribute.class).fetch();
//		StringBuilder sb = new StringBuilder();
//		sb.append("Query: '"+query+"'\n");
//		sb.append("Results: "+found.size()+"\n");
//		for (ImageAttribute attr : found) {
//			sb.append(String.format("%s %s %s\n",attr.image.getPath().toString(),attr.attribute,attr.value));
//		}
//		renderText(sb.toString());
//	}
}
