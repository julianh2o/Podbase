// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package services;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;
import access.VirtualAccessType;

import play.mvc.Util;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.ImageSetMembership;
import models.Paper;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.ProjectVisibleImage;
import models.User;

public class PermissionService {
	// Editing permissions
	public static Permission addPermission(User user, PermissionedModel model, AccessType access) {
		if (user == null) throw new IllegalArgumentException("User can't be null!");
		if (model == null) throw new IllegalArgumentException("Model can't be null!");
		if (access == null) throw new IllegalArgumentException("Access can't be null!");
		
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
		
		if (userHasVirtualAccess(User.getGuestAccount(),model,access)) return true;
		if (userHasVirtualAccess(User.getAuthenticatedAccount(),model,access)) return true;
		if (userHasVirtualAccess(user,model,access)) return true;
		
		return false;
	}
	
	public static boolean userHasVirtualAccess(User user, PermissionedModel model, AccessType access) {
		Set<VirtualAccessType> virtualAccess = getVirtualAccess(user, model);
		HashMap<AccessType, VirtualAccessType> virtualAccessMap = getVirtualAccessMap(virtualAccess);
		
		return virtualAccessMap.containsKey(access);
	}
	
	public static HashMap<AccessType, VirtualAccessType> getVirtualAccessMap(Set<VirtualAccessType> virtualAccess) {
		HashMap<AccessType, VirtualAccessType> virtualAccessMap = new HashMap<AccessType, VirtualAccessType>();
		for (VirtualAccessType access : virtualAccess) {
			virtualAccessMap.put(access.type, access);
		}
		
		return virtualAccessMap;
	}
	
	
	// Fetching permissions
	public static Permission getPermission(User user, PermissionedModel model, AccessType access) {
		Permission perm = Permission.find("byUserAndModelAndAccess", user, model, access).first();
		return perm;
	}
	
	public static Set<PermissionedModel> getModelsForUser(User user, AccessType access) {
		Set<AccessType> implicatingTypes = getImplicatingAccessTypes(access);
		
		Set<PermissionedModel> models = new HashSet<PermissionedModel>();
		for (AccessType type : implicatingTypes) {
			List<Permission> permissions = Permission.find("byUserAndAccess", user, type).fetch();
			models.addAll(getModelsFromPermissions(permissions));
		}
		return models;
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
	
	public static Set<AccessType> getResolvedAccess(User user, PermissionedModel model) {
		Set<AccessType> accessList = new HashSet<AccessType>();
		for (VirtualAccessType vat : getVirtualAccess(user, model)) {
			accessList.add(vat.type);
		}
		return accessList;
	}
	
	@Util
	public static Set<AccessType> getImplicatingAccessTypes(AccessType seekingAccess) {
		Set<AccessType> implicatingTypes = new HashSet<AccessType>();
		implicatingTypes.add(seekingAccess);
		
		for (Entry<AccessType,List<AccessType>> entry : AccessType.IMPLICATIONS.entrySet()) {
			if (entry.getValue().contains(seekingAccess)) {
				implicatingTypes.addAll(getImplicatingAccessTypes(entry.getKey()));
			}
		}
		
		return implicatingTypes;
	}
	
	public static Set<VirtualAccessType> getVirtualAccess(User user, PermissionedModel model) {
		Set<AccessType> accessList = getAccess(user,model);
		HashMap<String,VirtualAccessType> virtualAccess = new HashMap<String,VirtualAccessType>();
		for (AccessType access : accessList) {
			if (!virtualAccess.containsKey(access.name())) virtualAccess.put(access.name(),new VirtualAccessType(access));
			if (AccessType.IMPLICATIONS.containsKey(access)) {
				for (AccessType type : AccessType.IMPLICATIONS.get(access)) {
					if (virtualAccess.containsKey(type.name())) {
						virtualAccess.get(type.name()).addImplication(access);
					} else {
						VirtualAccessType newAccess = new VirtualAccessType(type,access);
						virtualAccess.put(type.name(),newAccess);
					}
				}
			}
		}
		return new HashSet<VirtualAccessType>(virtualAccess.values());
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
    	
    	Set<PermissionedModel> models = getModelsForUser(user,AccessType.VIEW_ALL_IMAGES);
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
    	
    	models = getModelsForUser(user,AccessType.VIEW_VISIBLE_IMAGES);
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
	    		if (ProjectVisibleImage.get(p, image) != null) {
	    		//System.out.println("project "+p.name);
	    			for (Directory dir : p.directories) {
	    				//System.out.println("testing "+dir.getPath().toString());
	    				if (image.getPath().startsWith(dir.getPath())) {
	    					return true;
	    				}
	    			}
	    		}
    		}
    	}
    	
    	return false;
	}

}
