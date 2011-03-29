import play.*;
import play.jobs.*;
import play.test.*;
 
import models.*;
 
@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
    	if (reset()) return;
        	
        // Check if the database is empty
        if(Project.count() == 0) {
            System.out.println("Loaded initial.yml");
	        Fixtures.load("initial.yml");
        }
    }
    
    public boolean reset() {
        Fixtures.deleteAll();
        Fixtures.load("initial.yml");
        new User("julianh2o@gmail.com", "secret").save();
        return true;
    }
 
}
