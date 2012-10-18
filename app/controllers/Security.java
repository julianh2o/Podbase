package controllers;

import java.util.List;

import access.Access;
import access.AccessType;
import access.PaperAccess;
import access.ProjectAccess;

import play.mvc.Before;
import play.mvc.Util;
import services.PermissionService;

import models.Paper;
import models.Project;
import models.User;
 
public class Security extends Secure.Security {
    @Before
    static void checkAccess() throws Throwable {
        User u = getUser();
        
        if (u.root) return;
        
        Access access = getActionAnnotation(Access.class);
        if (access != null) {
	        for (AccessType a : access.value()) {
	        	System.out.println("Security checking: "+a);
	        	boolean hasPermission = PermissionService.hasPermission(u, null, a);
	        	if (!hasPermission) forbidden();
	        }
        }
        
        ProjectAccess projectAccess = getActionAnnotation(ProjectAccess.class);
        if (projectAccess != null) {
        	Long projectId = params.get("projectId", Long.class);
        	Project project = null;
        	if (projectId != null) project = Project.findById(projectId);
        	if (project == null) forbidden();
        	
	        for (AccessType a : projectAccess.value()) {
	        	System.out.println("Project Security checking: "+a);
	        	boolean hasPermission = PermissionService.hasPermission(u, project, a);
	        	if (!hasPermission) forbidden();
	        }
        }
        
        PaperAccess paperAccess = getActionAnnotation(PaperAccess.class);
        if (paperAccess != null) {
        	Long paperId = params.get("paperId", Long.class);
        	Paper paper = null;
        	if (paperId != null) paper = Paper.findById(paperId);
        	if (paper == null) forbidden();
        	
	        for (AccessType a : paperAccess.value()) {
	        	System.out.println("Paper Security checking: "+a);
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
