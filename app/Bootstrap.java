import java.util.LinkedList;
import java.util.List;

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
	        
	        User guest = new User("guest","guest");
	        guest.guest = true;
	        guest.save();
	        
	        List<User> users = new LinkedList<User>();
	        User a = new User("julianh2o@gmail.com", "secret").save();
	        User b = new User("kittycasey@gmail.com", "secret").save();
	        User c = new User("boink@gmail.com", "secret").save();
	        
	        a.root = true;
	        a.save();
	        
	        Project p = (Project)Project.findAll().get(0);
	        
	        new UserPermission(p, guest,"visible").save(); //guest permission
	        
	        new UserPermission(p, a,"owner").save();
	        new UserPermission(p, a, "listed").save();
	        
	        new UserPermission(p, b, "listed").save();
	        
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
