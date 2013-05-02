package controllers;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import access.Access;
import access.AccessType;
import access.PaperAccess;
import access.ModelAccess;

import play.mvc.Before;
import play.mvc.Util;
import services.PathService;
import services.PermissionService;

import models.ImageSet;
import models.Paper;
import models.PermissionedModel;
import models.Podbase;
import models.Project;
import models.User;
 
public class Security extends Secure.Security {
    @Before
    static void checkAccess() throws Throwable {
        User u = getUser();
        
        if (u.isRoot()) return;
        
        Access access = getActionAnnotation(Access.class);
        if (access != null) {
	        for (AccessType a : access.value()) {
	        	boolean hasPermission = PermissionService.hasPermission(u,Podbase.getInstance(), a);
	        	if (!hasPermission) forbidden();
	        }
        }
        
        ModelAccess projectAccess = getActionAnnotation(ModelAccess.class);
        if (projectAccess != null) {
        	PermissionedModel model = params.get("project",PermissionedModel.class);
        	if (model != null && !model.isPersistent()) model = null;
        	
        	model = params.get("model",PermissionedModel.class);
        	if (model != null && !model.isPersistent()) model = null;
        
        	if (model == null) {
	        	Path path = params.get("path",Path.class);
	        	if (path != null) model = PathService.projectForPath(path);
        	}
	        	
        	if (model == null) forbidden();
        	
        	Set<AccessType> projectAccessSet = PermissionService.getResolvedAccess(u,model);
        	
	        for (AccessType a : projectAccess.value()) {
	        	boolean hasPermission = projectAccessSet.contains(a);
	        	if (!hasPermission) forbidden();
	        }
        }
        
        PaperAccess paperAccess = getActionAnnotation(PaperAccess.class);
        if (paperAccess != null) {
        	Paper paper = params.get("paper",Paper.class);
        	
        	if (paper == null) {
        		ImageSet set = params.get("imageset",ImageSet.class);
        		if (set != null) paper = set.paper;
        	}
        	
        	if (paper == null) forbidden();
        	
        	if (paper == null) forbidden();
	        for (AccessType a : paperAccess.value()) {
	        	boolean hasPermission = PermissionService.hasPermission(u, paper, a);
	        	if (!hasPermission) forbidden();
	        }
        }
    }
	
    @Util
    public static boolean authentify(String email, String password) {
        User user = User.find("byEmail", email).first();
        return user != null && user.authenticate(password);
    }
    
    @Util
    public static void checkPermission() {
    	String email = connected();
    	User user = User.find("byEmail", email).first();
    	if (user == null) redirect("/login");
    }
    
    @Util
    public static User getUser() {
    	User user = null;
    	if (Security.connected() != null) user = User.find("email", Security.connected()).first();
    	
    	if (user == null) return User.getGuestAccount();
    	return user;
    }

    @Util
	public static User logUserIn(User user) {
		session.put("username", user.email);		
		return user;
	}
}
