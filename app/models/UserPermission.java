package models;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

@Entity
public class UserPermission extends TimestampModel {
	@OneToOne
	public User user;
	
	@OneToOne
	public Project project;
	
	public String permission;
	
	//listed - is assigned to this project in the project listing page (this is required for all other permissions)
	//visible - this project is visible to this user
	//owner - this user is the owner of this project
	//managePermissions - this user can manage the permissions of this project
	public static String[] permissionList = new String[] {"listed","visible","owner","managePermissions"};

	public UserPermission(Project project, User user, String permission) {
		super();
		this.user = user;
		this.project = project;
		this.permission = permission;
	}
	
	public static UserPermission addPermission(Long projectId, Long userId, String permission) {
		Project project = Project.findById(projectId);
    	User user = User.findById(userId);
    	UserPermission permissionObject = new UserPermission(project,user,permission);
    	permissionObject.save();
    	return permissionObject;
	}
	
	public static List<String> getPermissionList() {
		return Arrays.asList(permissionList);
	}
	
	public String toString() {
		return user.email + "has "+permission+" with project "+project.name;
	}
	
	public static List<User> getUserList(List<UserPermission> permissions) {
		LinkedList<User> users = new LinkedList<User>();
		for (UserPermission permission : permissions) {
			users.add(permission.user);
		}
		return users;
	}
	
	public static List<Project> getProjectList(List<UserPermission> permissions) {
		LinkedList<Project> projects = new LinkedList<Project>();
		for (UserPermission permission : permissions) {
			projects.add(permission.project);
		}
		return projects;
	}
}