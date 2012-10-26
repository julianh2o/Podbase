package services;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;

import play.mvc.Util;

import models.Activation;
import models.Directory;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.User;

public class PermissionService {
	// Editing permissions
	public static Permission addPermission(User user, PermissionedModel model, AccessType access) {
		Permission permission = new Permission(user,model,access);
		ParentController.debug(user,model,access);
		permission.save();
		return permission;
	}
	
	public static boolean removePermission(User user, PermissionedModel model, AccessType access) {
		Permission permission = Permission.find("byUserAndModelAndAccess",user,model,access).first();
		if (permission == null) return false;
		
		permission.delete();
		return true;
	}
	
	public static void togglePermission(User user, Project project, AccessType access, boolean value) {
    	Permission perm = getPermission(user,project,access);
    	if (perm == null && value) {
    		addPermission(user, project, access);
    	} else if (perm != null && !value) {
    		perm.delete();
    	}
	}
	
	
	
	// Inherited permissions
	public static boolean hasInheritedAccess(User user, PermissionedModel model, AccessType access) {
		if (hasPermission(User.getGuestAccount(),model,access)) return true;
		if (hasPermission(User.getAuthenticatedAccount(),model,access)) return true;
		if (hasPermission(user,model,access)) return true;
		
		return false;
	}
	
	
	// Fetching permissions
	public static Permission getPermission(User user, PermissionedModel model, AccessType access) {
		Permission perm = Permission.find("byUserAndModelAndAccess", user, model, access).first();
		return perm;
	}
	
	public static List<PermissionedModel> getModelsForUser(User user, AccessType access) {
		List<Permission> permissions = Permission.find("byUserAndAccess", user, access).fetch();
		return getModelsFromPermissions(permissions);
	}
	
	public static List<User> getUsersForModel(PermissionedModel model, AccessType access) {
		List<Permission> permissions = Permission.find("byModelAndAccess", model, access).fetch();
		return getUsersFromPermissions(permissions);
	}
	
	public static List<Permission> getPermissions(User user, PermissionedModel model) {
		List<Permission> permissions = Permission.find("byUserAndModel", user, model).fetch();
		return permissions;
	}
	
	public static List<AccessType> getStringPermissions(User user, PermissionedModel model) {
		return getAccessFromPermissions(getPermissions(user,model));
	}
	
	public static boolean hasPermission(User user, PermissionedModel model, AccessType access) {
		Permission perm = Permission.find("byUserAndModelAndAccess", user, model, access).first();
		return perm != null;
	}
	
	
	// Converting datastructures
    public static <T> List<T> filter(List<PermissionedModel> models, Class<T> type) {
		List<T> list = new LinkedList<T>();
		for (PermissionedModel model : models) {
			if (model.getClass().equals(type)) {
				list.add((T)model);
			}
		}
		return list;
	}
	
	public static List<PermissionedModel> getModelsFromPermissions(List<Permission> permissions) {
		LinkedList<PermissionedModel> models = new LinkedList();
		for (Permission p : permissions) {
			models.add(p.model);
		}
		return models;
	}
	
	public static List<User> getUsersFromPermissions(List<Permission> permissions) {
		LinkedList<User> users = new LinkedList();
		for (Permission p : permissions) {
			users.add(p.user);
		}
		return users;
	}
	
	public static List<AccessType> getAccessFromPermissions(List<Permission> permissions) {
		LinkedList<AccessType> names = new LinkedList();
		for (Permission p : permissions) {
			names.add(p.access);
		}
		return names;
	}
}
