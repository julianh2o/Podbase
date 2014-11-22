// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
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
	static void db(Object s) {
		//Uncomment to debug
		//System.out.println(s);
	}
	
    @Before
    static void checkAccess() throws Throwable {
        User u = getUser();
        
        if (u.isRoot()) return;
        
        Access access = getActionAnnotation(Access.class);
        if (access != null) {
        	db(request.url+": "+access);
	        for (AccessType a : access.value()) {
	        	boolean hasPermission = PermissionService.hasInheritedAccess(u,Podbase.getInstance(), a);
	        	if (!hasPermission) redirectToLogin();
	        }
        }
        
        ModelAccess modelAccess = getActionAnnotation(ModelAccess.class);
        if (modelAccess != null) {
        	db(request.url+": "+modelAccess);
        	PermissionedModel model = params.get("project",PermissionedModel.class);
        	if (model != null && !model.isPersistent()) model = null;
        	
        	if (model == null && params.get("projectId",Long.class) != null) model = Project.findById(params.get("projectId",Long.class));
        	
        	if (model == null) model = params.get("model",PermissionedModel.class);
        	if (model != null && !model.isPersistent()) model = null;
        
        	if (model == null) {
	        	Path path = params.get("path",Path.class);
	        	if (path != null) model = PathService.projectForPath(path);
        	}
        	
        	db("  Security Model: "+model);
	        	
        	if (model == null) redirectToLogin();
        	
        	Set<AccessType> projectAccessSet = PermissionService.getResolvedAccess(u,model);
        	
	        for (AccessType a : modelAccess.value()) {
	        	boolean hasPermission = projectAccessSet.contains(a);
	        	if (!hasPermission) redirectToLogin("You do not have view permission for "+model.toString());
	        }
        }
        
        PaperAccess paperAccess = getActionAnnotation(PaperAccess.class);
        if (paperAccess != null) {
        	db(request.url+": "+paperAccess);
        	Paper paper = params.get("paper",Paper.class);
        	
        	if (paper == null && params.get("paperId",Long.class) != null) paper = Paper.findById(params.get("paperId",Long.class));
        	
        	if (paper == null) {
        		ImageSet set = params.get("imageset",ImageSet.class);
        		if (set != null) paper = set.paper;
        	}
        	
        	db("  Security Model: "+paper);
        	
        	if (paper == null) redirectToLogin();
        	
	        for (AccessType a : paperAccess.value()) {
	        	boolean hasPermission = PermissionService.hasInheritedAccess(u, paper, a);
	        	if (!hasPermission) redirectToLogin();
	        }
        }
    }
    
	protected static void redirectToLogin() {
		redirectToLogin("You do not have sufficient priveledges to access this URL. Try logging in or navigating from the home page.");
	}
	protected static void redirectToLogin(String message) {
		if (request.isAjax()) forbidden(message);
		
		if (!getUser().isGuest()) forbidden(message);
		
		String url = "GET".equals(request.method) ? request.url : "/";
		flash.put("url", url);
		redirect("/login");
	}
	
	
    @Util
    public static boolean authenticate(String email, String password) {
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
