package jobs;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import controllers.ImageBrowser;

import access.AccessType;

import play.*;
import play.jobs.*;
import play.test.*;
 
import models.*;
 
@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
    	if (true) {
	        Fixtures.deleteDatabase();
	        //Fixtures.loadModels("initial.yml");
	        
	        User guest = new User("guest",null);
	        guest.save();
	        
	        User authenticated = new User("authenticated",null);
	        authenticated.save();
	        
	        User a = new User("julianh2o@gmail.com", "secret").save();
	        User b = new User("kittycasey@gmail.com", "secret").save();
	        User c = new User("danh@pbrc.hawaii.edu", "lucifer").save();
	        
	        a.root = true;
	        a.save();
	        
	        c.root = true;
	        c.save();
	        
	        //Guest user permissions
	        new Permission(a,null,AccessType.CREATE_PROJECT).save();
	        new Permission(a,null,AccessType.CREATE_PAPER).save();
	        
	        //Default project
	        Project project = new Project("Default Project").save();
	        new Permission(c,project,AccessType.OWNER).save();
	        
	        List<User> users = User.findAll();
	        for (User u : users) {
		        new Permission(u,project,AccessType.VISIBLE).save();
		        new Permission(u,project,AccessType.LISTED).save();
	        }
	        
	        for (File f : ImageBrowser.getRootImageDirectoryFile().listFiles()) {
	        	project.addDirectory("/"+f.getName());
	        }
    	} else {
	        // Check if the database is empty
	        if(Project.count() == 0) {
	            System.out.println("Loading initial.yml");
		        Fixtures.loadModels("initial.yml");
		        new User("julianh2o@gmail.com", "secret").save();
	        }
    	}
    }
}
