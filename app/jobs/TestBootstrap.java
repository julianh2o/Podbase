// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package jobs;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import controllers.ImageBrowser;

import access.AccessType;

import play.*;
import play.jobs.*;
import play.test.*;
import services.PathService;
import services.PermissionService;
 
import models.*;
 
//@OnApplicationStart
public class TestBootstrap extends Job {
	private final String DEFAULT_ROOT_PASSWORD = "password";
	
    public void doJob() {
		if (!(Play.id.equals("dev") || Play.id.equals("setup"))) {
			System.out.println("Skipping Bootstrap!");
			return;
		}
		
		//System.out.println("Clearing database and bootstrapping.");
        //Fixtures.deleteDatabase();
		
		if (Podbase.findAll().size() > 0) {
			System.out.println("Podbase is already bootstrapped.");
			return;
		}
        
		System.out.println("Bootstrapping database!");
        new Podbase().save();
        
        String rootPassword = Play.configuration.getProperty("podbase.root.password",DEFAULT_ROOT_PASSWORD);
        if (rootPassword.equals(DEFAULT_ROOT_PASSWORD)) System.out.println("Warning: Using default root password!");
        User root = new User("root",rootPassword);
        root.special = true;
        root.save();
        
        User guest = new User("guest",null);
        guest.special = true;
        guest.save();
        
        User authenticated = new User("authenticated",null);
        authenticated.special = true;
        authenticated.save();
        
        User a = new User("julianh2o@gmail.com", "secret").save();
        User b = new User("kittycasey@gmail.com", "secret").save();
        User c = new User("danh@pbrc.hawaii.edu", "lucifer").save();
        
        //Guest user permissions
        new Permission(a,null,AccessType.CREATE_PROJECT).save();
        new Permission(a,null,AccessType.CREATE_PAPER).save();
        
        List<User> users = new LinkedList<User>();
        users.add(a);
        users.add(b);
        users.add(c);
        
        for (File f : PathService.getRootImageDirectory().toFile().listFiles()) {
        	if (f.isDirectory()) {
	        	System.out.println("Creating project: "+f.getName());
		        Project project = new Project(f.getName()).save();
		        
		        new Permission(c,project,AccessType.OWNER).save();
		        new Permission(authenticated,project,AccessType.PARTICIPANT).save();
		        new Permission(authenticated,project,AccessType.LISTED).save();
		        
		        Template template = new Template(project, f.getName()+"_demo_template").save();
		        template.addAttribute("Image Name","The name of the image");
		        template.addAttribute("Species","The species of the animal");
		        template.addAttribute("Magnification","The magnification that this image was taken at");
		        
		        addUsersToProject(project, users);
		        
	        	project.addDirectory("/"+f.getName());
        	}
        }
        
        Project sandbox = Project.find("byName", "GuestSandbox").first();
        if (sandbox != null) {
	        PermissionService.addPermission(guest, sandbox, AccessType.OWNER);
	        PermissionService.addPermission(guest, sandbox, AccessType.LISTED);
	        PermissionService.addPermission(guest, sandbox, AccessType.PARTICIPANT);
	        PermissionService.addPermission(guest, sandbox, AccessType.EDIT_ANALYSIS_METADATA);
        } else {
        	System.out.println("Sandbox project not found!");
        }
        
        Project pub = Project.find("byName", "Public").first();
        if (pub != null) {
	        PermissionService.addPermission(guest, pub, AccessType.LISTED);
	        PermissionService.addPermission(guest, pub, AccessType.PARTICIPANT);
        } else {
        	System.out.println("Public project not found!");
        }
    }
    
    private void addUsersToProject(Project project, List<User> users) {
        for (User u : users) {
	        new Permission(u,project,AccessType.LISTED).save();
	        new Permission(u,project,AccessType.PARTICIPANT).save();
	        new Permission(u,project,AccessType.EDIT_ANALYSIS_METADATA).save();
        }
    }
    
}
