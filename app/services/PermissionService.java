package services;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;

import play.mvc.Util;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.ImageSetMembership;
import models.Paper;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.User;

public class PermissionService {
	// Editing permissions
	public static Permission addPermission(User user, PermissionedModel model, AccessType access) {
		Permission permission = new Permission(user,model,access);
		permission.save();
		return permission;
	}
	
	public static boolean removePermission(User user, PermissionedModel model, AccessType access) {
		Permission permission = Permission.find("byUserAndModelAndAccess",user,model,access).first();
		if (permission == null) return false;
		
		permission.delete();
		return true;
	}
	
	public static void togglePermission(User user, PermissionedModel model, AccessType access, boolean value) {
    	Permission perm = getPermission(user,model,access);
    	if (perm == null && value) {
    		addPermission(user, model, access);
    	} else if (perm != null && !value) {
    		perm.delete();
    	}
	}
	
	
	
	// Inherited permissions
	public static boolean hasInheritedAccess(User user, PermissionedModel model, AccessType access) {
		if (user.isRoot()) return true;
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
	
	public static Set<PermissionedModel> getModelsForUser(User user, AccessType access) {
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
	
	public static List<String> getStringPermissions(User user, PermissionedModel model) {
		List<String> strings = new LinkedList<String>();
		Set<AccessType> accessTypes = getAccessFromPermissions(getPermissions(user,model));
		for (AccessType accessType : accessTypes) {
			strings.add(accessType.toString());
		}
		return strings;
	}
	
	public static Set<AccessType> getAccess(User user, PermissionedModel model) {
		return getAccessFromPermissions(getPermissions(user,model));
	}
	
	public static Set<AccessType> getVirtualAccess(User user, PermissionedModel model) {
		Set<AccessType> accessList = getAccess(user,model);
		Set<AccessType> virtualAccess = new HashSet<AccessType>();
		for (AccessType access : accessList) {
			virtualAccess.add(access);
			if (AccessType.IMPLICATIONS.containsKey(access)) {
				virtualAccess.addAll(AccessType.IMPLICATIONS.get(access));
			}
		}
		return virtualAccess;
	}
	
	public static boolean hasPermission(User user, PermissionedModel model, AccessType access) {
		Permission perm = Permission.find("byUserAndModelAndAccess", user, model, access).first();
		return perm != null;
	}
	
	
	// Converting datastructures
    public static <T> Set<T> filter(Set<PermissionedModel> models, Class<T> type) {
    	Set<T> list = new HashSet<T>();
		for (PermissionedModel model : models) {
			if (model.getClass().equals(type)) {
				list.add((T)model);
			}
		}
		return list;
	}
	
	public static Set<PermissionedModel> getModelsFromPermissions(List<Permission> permissions) {
		Set<PermissionedModel> models = new HashSet();
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
	
	public static Set<AccessType> getAccessFromPermissions(List<Permission> permissions) {
		Set<AccessType> names = new HashSet<AccessType>();
		for (Permission p : permissions) {
			names.add(p.access);
		}
		return names;
	}
	
	//TODO make this more efficient? (caching?)
    @Util
	public static boolean userCanAccessPath(User user, Path path) {
    	if (user.isRoot()) return true;
    	
    	DatabaseImage image = DatabaseImage.forPath(path);
    	Set<PermissionedModel> models = getModelsForUser(user,AccessType.VISIBLE);
    	for(PermissionedModel model : models) {
    		if (model instanceof Paper) {
    			Paper p = (Paper)model;
	    		//System.out.println("paper "+p.name);
    			for (ImageSetMembership entry : p.imageset.images) {
    				if (entry.image.equals(image)) {
    					return true;
    				}
    			}
    		} else if (model instanceof Project){
    			Project p = (Project)model;
	    		//System.out.println("project "+p.name);
    			for (Directory dir : p.directories) {
    				//System.out.println("testing "+dir.getPath().toString());
    				if (image.getPath().startsWith(dir.getPath())) {
    					return true;
    				}
    			}
    		}
    	}
    	
    	return false;
	}

}
