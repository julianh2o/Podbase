package controllers;

import java.util.Arrays;
import java.util.List;

import access.Access;
import access.AccessType;
import access.ProjectAccess;

import play.mvc.Util;
import play.mvc.With;
import services.PermissionService;

import models.Directory;
import models.Permission;
import models.Project;
import models.User;

@With(Security.class)
public class ProjectController extends ParentController {
    public static void getProjects() {
    	User user = Security.getUser();
    	
    	if (user.root)renderJSON(Project.findAll());
    	List<Project> projects = PermissionService.filter(PermissionService.getModelsForUser(user, AccessType.VISIBLE), Project.class);
    	
    	renderJSON(projects);
    }
    
    @ProjectAccess(AccessType.VISIBLE)
    public static void getProject(Project project) {
    	renderJSON(project);
    }
    
    @Access(AccessType.CREATE_PROJECT)
    public static void createProject(String name) {
    	User user = Security.getUser();
    	
    	Project project = new Project(name);
    	project.save();
    	
    	PermissionService.togglePermission(user,project,AccessType.OWNER,true);
    	PermissionService.togglePermission(user,project,AccessType.VISIBLE,true);
    	PermissionService.togglePermission(user,project,AccessType.LISTED,true);
    	
    	ok();
    }
    
    @ProjectAccess(AccessType.EDITOR)
    public static void setDataMode(Project project, boolean dataMode) {
    	project.dataMode = dataMode;
    	ok();
    }
    
    @Access(AccessType.DELETE_PROJECT)
    public static void deleteProject(Project project) {
    	project.delete();
    	ok();
    }
    
    @ProjectAccess(AccessType.PROJECT_MANAGE_DIRECTORIES)
    public static void addDirectory(Project project, String path) {
    	project.addDirectory(path);
    	ok();
    }
    
    @ProjectAccess(AccessType.PROJECT_MANAGE_DIRECTORIES)
    public static void removeDirectory(Directory directory) {
    	directory.delete();
    	ok();
    }
    
}
