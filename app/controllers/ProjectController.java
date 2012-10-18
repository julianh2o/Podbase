package controllers;

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
    	List<Project> projects = PermissionService.filter(PermissionService.getModelsForUser(user, AccessType.PROJECT_VISIBLE), Project.class);
    	
    	renderJSON(projects);
    }
    
    @ProjectAccess(AccessType.PROJECT_VISIBLE)
    public static void getProject(Project project) {
    	renderJSON(project);
    }
    
    @Access(AccessType.CREATE_PROJECT)
    public static void createProject(String name) {
    	User user = Security.getUser();
    	
    	Project project = new Project(name);
    	project.save();
    	
    	PermissionService.togglePermission(user,project,AccessType.PROJECT_OWNER,true);
    	PermissionService.togglePermission(user,project,AccessType.PROJECT_LISTED,true);
    	
    	ok();
    }
    
    @ProjectAccess(AccessType.PROJECT_EDIT_METADATA)
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
    
    public static void getListedUsers(Project project) {
    	List<User> users = PermissionService.getUsersForModel(project,AccessType.PROJECT_LISTED);
    	renderJSON(users);
    }
    
    public static void getUserProjectAccess(Project project) {
    	User user = Security.getUser();
    	List<Permission> permissions = PermissionService.getPermissions(user, project);
    	renderJSON(permissions);
    }
    
    public static void getAccess(Project project) {
    	User user = Security.getUser();
    	List<Permission> permissions = PermissionService.getPermissions(user, project);
    	renderJSON(PermissionService.getAccessFromPermissions(permissions));
    }
    
    public static void addUserByEmail(Project project, String email) {
    	User user = User.find("byEmail", email).first();
    	
    	PermissionService.togglePermission(user,project,AccessType.PROJECT_LISTED,true);
    	
    	ok();
    }
    
    public static void removeUser(Project project, User user) {
    	List<Permission> permissions = PermissionService.getPermissions(user, project);
    	for(Permission perm : permissions) {
    		perm.delete();
    	}
    	
    	ok();
    }
    
    public static void setPermission(Project project, User user, String permission, boolean value) {
    	AccessType access = AccessType.valueOf(permission);
    	
    	PermissionService.togglePermission(user,project,access,value);
    	
    	ok();
    }
}
