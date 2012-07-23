package controllers;

import java.util.List;

import models.Directory;
import models.Project;
import models.User;
import models.UserPermission;

public class ProjectManager extends ParentController {
    public static void index(long projectId) {
    	Project project = Project.findById(projectId);
    	List<User> users = User.findAll();
    	List<String> permissionsList = UserPermission.getPermissionList();
    	render(project, users, permissionsList);
    }
    
	public static void script() {
		renderTemplate("ProjectManager/script.js");
	}
}
