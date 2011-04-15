package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
    	render();
    }
    
    public static void showUsers() {
    	Security.checkPermission();
    	List<User> users = User.findAll();
    	render(users);
    }
    
    public static void listProjects() {
    	List<Project> projects = Project.findAll();
    	render(projects);
    }
    
    public static void showProject(long projectId) {
    	Project project = Project.findById(projectId);
    	render(project);
    }
    
    public static void createProject(String name) {
    	Project project = new Project(name);
    	project.save();
    	listProjects();
    }
    
    public static void deleteProject(long projectId) {
    	Project project = Project.findById(projectId);
    	project.delete();
    	listProjects();
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
    
    public static void logout() {
    	try {
    		Secure.logout();
    	} catch (Throwable t) {
    		error();
    	}
    	index();
    }
}
