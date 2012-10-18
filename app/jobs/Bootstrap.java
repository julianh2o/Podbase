package jobs;
import java.util.LinkedList;
import java.util.List;

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
	        Fixtures.loadModels("initial.yml");
	        
	        User guest = new User("guest",null);
	        guest.save();
	        
	        User authenticated = new User("authenticated",null);
	        authenticated.save();
	        
	        List<User> users = new LinkedList<User>();
	        User a = new User("julianh2o@gmail.com", "secret").save();
	        User b = new User("kittycasey@gmail.com", "secret").save();
	        User c = new User("boink@gmail.com", "secret").save();
	        User d = new User("danh@pbrc.hawaii.edu", "lucifer").save();
	        
	        a.root = true;
	        a.save();
	        
	        Project p = (Project)Project.findAll().get(0);
	        Paper pa = (Paper)Paper.findAll().get(0);
	        
	        Permission perm = new Permission(a,p,AccessType.PROJECT_VISIBLE).save();
	        Permission perm1 = new Permission(a,pa,AccessType.PROJECT_OWNER).save();
	        Permission perm2 = new Permission(a,null,AccessType.CREATE_PROJECT).save();
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
