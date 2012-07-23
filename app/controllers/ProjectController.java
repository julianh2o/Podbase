package controllers;

import java.util.List;

import models.Directory;
import models.Project;
import models.User;
import models.UserPermission;

public class ProjectController extends ParentController {
    public static void getProjects() {
    	List<Project> projects = Project.findAll();
    	renderJSON(projects);
    }
    
    public static void getProject(long projectId) {
    	Project project = Project.findById(projectId);
    	renderJSON(project);
    }
    
    public static void createProject(String name) {
    	Project project = new Project(name);
    	project.save();
    	ok();
    }
    
    public static void deleteProject(long projectId) {
    	Project project = Project.findById(projectId);
    	project.delete();
    	ok();
    }
    
    public static void addDirectory(long projectId, String path) {
    	Project project = Project.findById(projectId);
    	project.addDirectory(path);
    	ok();
    }
    
    public static void removeDirectory(long directoryId) {
    	Directory directory = Directory.findById(directoryId);
    	directory.delete();
    	ok();
    }
    
    public static void setUserPermission(Long projectId, Long userId, String permission, boolean value) {
		Project project = Project.findById(projectId);
    	User user = User.findById(userId);
    	
    	UserPermission userPermission = UserPermission.find("byProjectAndUserAndPermission", project, user, permission).first();
    	if (userPermission == null && value) {
	    	UserPermission newPermission = new UserPermission(project,user,permission);
	    	newPermission.save();
    	} else if (userPermission != null && !value) {
    		userPermission.delete();
    	}
    	ProjectManager.index(projectId);
    }
}
