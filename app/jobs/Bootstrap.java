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
 
@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
    	if (true) {
	        Fixtures.deleteDatabase();
	        //Fixtures.loadModels("initial.yml");
	        
	        new Podbase().save();
	        
	        User root = new User("root","lucifer");
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
			        Project project = new Project(f.getName()).save();
			        
			        new Permission(c,project,AccessType.OWNER).save();
			        new Permission(authenticated,project,AccessType.VISIBLE).save();
			        new Permission(authenticated,project,AccessType.LISTED).save();
			        
			        Template template = new Template(project, f.getName()+"_demo_template").save();
			        template.addAttribute("Image Name");
			        template.addAttribute("Species");
			        template.addAttribute("Magnification");
			        
			        addUsersToProject(project, users);
			        
		        	project.addDirectory("/"+f.getName());
	        	}
	        }
	        
	        Project sandbox = Project.find("byName", "GuestSandbox").first();
	        PermissionService.addPermission(guest, sandbox, AccessType.OWNER);
	        PermissionService.addPermission(guest, sandbox, AccessType.LISTED);
	        PermissionService.addPermission(guest, sandbox, AccessType.VISIBLE);
	        PermissionService.addPermission(guest, sandbox, AccessType.EDITOR);
	        
	        Project pub = Project.find("byName", "Public").first();
	        PermissionService.addPermission(guest, pub, AccessType.LISTED);
	        PermissionService.addPermission(guest, pub, AccessType.VISIBLE);
    	} else {
	        // Check if the database is empty
	        if(Project.count() == 0) {
	            System.out.println("Loading initial.yml");
		        Fixtures.loadModels("initial.yml");
		        new User("julianh2o@gmail.com", "secret").save();
	        }
    	}
    }
    
    private void addUsersToProject(Project project, List<User> users) {
        for (User u : users) {
	        new Permission(u,project,AccessType.LISTED).save();
	        new Permission(u,project,AccessType.VISIBLE).save();
	        new Permission(u,project,AccessType.EDITOR).save();
        }
    }
    
}
