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
	        users.add((User)new User("julianh2o@gmail.com", "secret").save());
	        users.add((User)new User("kittycasey@gmail.com", "secret").save());
	        users.add((User)new User("boink@gmail.com", "secret").save());
	        users.get(0).root = true;
	        users.get(0).save();
	        
	        Project p = (Project)Project.findAll().get(0);
	        new UserPermission(p,guest,"visible").save();
	        
	        new UserPermission(p, null, "visible").save();
	        for(User u : users) {
		        new UserPermission(p, u, "visible").save();
		        new UserPermission(p, u, "listed").save();
	        }
	        
	        User julian = users.get(0);
	        new UserPermission(p,julian,"owner").save();
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
