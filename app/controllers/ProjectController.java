package controllers;

import java.util.List;

import models.Directory;
import models.Project;
import models.User;
import models.UserPermission;

public class ProjectController extends ParentController {
    public static void getProjects() {
    	User user = Security.getUser();
    	
    	List<Project> projects = user.getProjectsWithAnyOf("visible","owner");
    	
    	renderJSON(projects);
    }
    
    public static void getProject(long projectId) {
    	Project project = Project.findById(projectId);
    	renderJSON(project);
    }
    
    public static void createProject(String name) {
    	Project project = new Project(name);
    	project.save();
    	
    	User user = Security.getUser();
    	setUserPermission(project.id,user.id,"owner",true);
    	setUserPermission(project.id,user.id,"listed",true);
    	
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
    
    public static void getUserPermissions(Long projectId) {
    	Project project = Project.findById(projectId);
    	List<User> users = project.getUsersWithPermission("listed");
    	renderJSON(users);
    }
    
    public static void getAllProjectPermissions() {
    	renderJSON(UserPermission.permissionList);
    }
    
    public static void addUserByEmail(Long projectId, String email) {
    	User user = User.find("byEmail", email).first();
    	setUserPermission(projectId,user.id,"listed",true);
    }
    
    public static void removeUser(Long projectId, Long userId) {
		Project project = Project.findById(projectId);
    	User user = User.findById(userId);
    	
    	List<UserPermission> userPermission = UserPermission.find("byProjectAndUser", project, user).fetch();
    	for(UserPermission perm : userPermission) {
    		perm.delete();
    	}
    	
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
    	ok();
    }
    
    public static void getPermissionsForProject(Long projectId) {
    	Project project = Project.findById(projectId);
    	renderJSON(project.getPermissionsForUser(Security.getUser()));
    }
    
    public static void getAccess(Long projectId) {
    	Project project = Project.findById(projectId);
    	renderJSON(project.getUserAccess(Security.getUser()));
    }
}
