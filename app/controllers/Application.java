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
    
    public static void showProjects() {
    	List<Project> projects = Project.findAll();
    	render(projects);
    }
    
    public static void showProject(Long id) {
    	Project project = Project.findById(id);
    	render(project);
    }
    
    public static void createProject(String name) {
    	Project project = new Project(name);
    	project.save();
    	showProjects();
    }
    
    public static void deleteProject(Long id) {
    	Project project = Project.findById(id);
    	project.delete();
    	showProjects();
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
