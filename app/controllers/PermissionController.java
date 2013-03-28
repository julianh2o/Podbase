package controllers;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import notifiers.Email;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;

import play.mvc.Util;
import play.mvc.With;
import services.PermissionService;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.Permission;
import models.PermissionedModel;
import models.Podbase;
import models.User;

@With(Security.class)
public class PermissionController extends ParentController {
    public static void setPermission(PermissionedModel model, User user, String permission, boolean value) {
    	AccessType access = AccessType.valueOf(permission);
    	
    	PermissionService.togglePermission(user,model,access,value);
    	ok();
    }
    
    public static void setUserPermission(User user, String permission, boolean value) {
    	AccessType access = AccessType.valueOf(permission);
    	
    	PermissionService.togglePermission(user,null,access,value);
    	ok();
    }
    
    public static void getListedUsers(PermissionedModel model) {
    	List<User> users = PermissionService.getUsersForModel(model,AccessType.LISTED);
    	renderJSON(users);
    }
    
    public static void getCurrentUserPermissions() {
    	renderJSON(getAccessForUser(Podbase.getInstance(),Security.getUser()));
    }
    
    public static void getCurrentPodbase() {
    	renderJSON(Podbase.getInstance());
    }
    
    public static void getUserAccess(PermissionedModel model, User user) {
    	renderJSON(getAccessForUser(model,user));
    }
    
    public static void getAccess(PermissionedModel model) {
    	User user = Security.getUser();
    	renderJSON(getAccessForUser(model,user));
    }
    
    public static void addUserByEmail(PermissionedModel model, String email) {
    	User user = User.find("byEmail", email).first();
    	if (user == null) error("User not found");
    	
    	PermissionService.togglePermission(user,model,AccessType.LISTED,true);
    	PermissionService.togglePermission(user,model,AccessType.VISIBLE,true);
    	
    	ok();
    }
    
    public static void removeUser(PermissionedModel model, User user) {
    	List<Permission> permissions = PermissionService.getPermissions(user, model);
    	for(Permission perm : permissions) {
    		perm.delete();
    	}
    	
    	ok();
    }
    
    @Util
    public static Set<AccessType> getAccessForUser(PermissionedModel model, User user) {
    	if (user == null) user = Security.getUser();
    	if (user.isRoot()) {
    		return new HashSet(Arrays.asList(AccessType.values()));
    	}
    	Set<AccessType> access = PermissionService.getVirtualAccess(user, model);
    	return access;
    }
    
    public static void getAccessTypes() {
    	renderJSON(AccessType.values());
    }
    
}
