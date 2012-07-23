package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends ParentController {

    public static void index() {
    	render();
    }
    
	public static void script() {
		renderTemplate("Application/script.js");
	}
	
	public static void loadJavascript(String path) {
		renderTemplate("/public/javascripts/"+path);
	}
    
    public static void showUsers() {
    	List<User> users = User.findAll();
    	render(users);
    }
    
    public static void getProjects() {
    	User user = Security.getUser();
    	List<Project> projects = null;
    	if (user == null) {
    		projects = UserPermission.getProjectList(Project.getProjectsWithGuestPermission("visible"));
    	} else {
	    	projects = Security.getUser().getProjectsWithPermission("visible");
    	}
    	renderJSON(projects);
    }
    
    @Deprecated
    public static void listProjects() {
    	User user = Security.getUser();
    	List<Project> projects = null;
    	if (user == null) {
    		projects = UserPermission.getProjectList(Project.getProjectsWithGuestPermission("visible"));
    	} else {
	    	projects = Security.getUser().getProjectsWithPermission("visible");
    	}
    	render(projects);
    }
    
    public static void showProject(long projectId) {
    	Project project = Project.findById(projectId);
    	render(project);
    }
    
    public static void createProject(String name) {
    	Project project = new Project(name);
    	project.save();
    	getProjects();
    }
    
    public static void deleteProject(long projectId) {
    	Project project = Project.findById(projectId);
    	project.delete();
    	getProjects();
    }
    
    public static void addDirectory(long projectId, String path) {
    	Project project = Project.findById(projectId);
    	project.addDirectory(path);
    	showProject(project.id);
    }
    
    public static void removeDirectory(long directoryId) {
    	Directory directory = Directory.findById(directoryId);
    	directory.delete();
    	showProject(directory.project.id);
    }
}
