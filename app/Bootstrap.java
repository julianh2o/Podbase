import play.*;
import play.jobs.*;
import play.test.*;
 
import models.*;
 
@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
    	if (true) {
	        Fixtures.deleteAll();
	        Fixtures.load("initial.yml");
	        User u = new User("julianh2o@gmail.com", "secret").save();
	        Project p = (Project)Project.findAll().get(0);
	        new UserPermission(p, u, "visible").save();
	        new UserPermission(p, u, "listed").save();
    	} else {
	        // Check if the database is empty
	        if(Project.count() == 0) {
	            System.out.println("Loading initial.yml");
		        Fixtures.load("initial.yml");
		        new User("julianh2o@gmail.com", "secret").save();
	        }
    	}
    }
}
