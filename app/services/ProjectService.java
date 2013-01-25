package services;

import java.util.List;

import access.AccessType;
import controllers.Security;
import models.Project;
import models.User;

public class ProjectService {
	public static List<Project> getVisibleProjects() {
    	User user = Security.getUser();
    	if (user.isRoot()) return Project.findAll();
    	
    	return PermissionService.filter(PermissionService.getModelsForUser(user, AccessType.VISIBLE), Project.class);
	}
	
	public static Project createProject(String name) {
    	User user = Security.getUser();
    	
    	Project project = new Project(name);
    	project.save();
    	
    	project.addDirectory("/"+name);
    	
    	PermissionService.togglePermission(user,project,AccessType.OWNER,true);
    	PermissionService.togglePermission(user,project,AccessType.VISIBLE,true);
    	PermissionService.togglePermission(user,project,AccessType.LISTED,true);
    	
    	return project;
	}
}
