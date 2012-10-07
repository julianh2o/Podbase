package controllers;

import play.*;
import play.db.jpa.JPA;
import play.mvc.*;

import java.util.*;

import javax.persistence.Query;

import models.*;

public class Application extends ParentController {

    public static void index() {
    	render();
    }
    
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
}
