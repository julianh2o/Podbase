// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import access.AccessType;
import controllers.Security;
import models.Project;
import models.User;

public class ProjectService {
	public static Set<Project> getVisibleProjects() {
    	User user = Security.getUser();
    	if (user.isRoot()) return new HashSet(Project.findAll());
    	
    	return PermissionService.filter(PermissionService.getModelsForUser(user, AccessType.LISTED), Project.class);
	}
	
	public static Project createProject(String name) {
    	User user = Security.getUser();
    	
    	Project project = new Project(name);
    	project.save();
    	
    	project.addDirectory("/"+name);
    	
    	PermissionService.togglePermission(user,project,AccessType.OWNER,true);
    	
    	return project;
	}
}
