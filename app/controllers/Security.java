package controllers;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import access.Access;
import access.AccessType;
import access.PaperAccess;
import access.ProjectAccess;

import play.mvc.Before;
import play.mvc.Util;
import services.PathService;
import services.PermissionService;

import models.ImageSet;
import models.Paper;
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
	        	boolean hasPermission = PermissionService.hasPermission(u, null, a);
	        	if (!hasPermission) forbidden();
	        }
        }
        
        ProjectAccess projectAccess = getActionAnnotation(ProjectAccess.class);
        if (projectAccess != null) {
        	Project project = params.get("project",Project.class);
        	if (!project.isPersistent()) project = null;
        
        	if (project == null) {
	        	Path path = params.get("path",Path.class);
	        	project = PathService.projectForPath(path);
        	}
	        	
        	if (project == null) forbidden();
        	
        	Set<AccessType> projectAccessSet = PermissionService.getVirtualAccess(u,project);
        	
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
	        	System.out.println("Checking access: "+a);
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
