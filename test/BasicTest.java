import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteAll();
    }

    
    @Test
    public void userCreation() {
    	User u = new User("julianh2o@gmail.com","password");
    	u.save();
    	
		User find = User.find("byEmail", "julianh2o@gmail.com").first();
		
		assertNotNull(find);
		assertEquals(u.email, find.email);
    }
    
    @Test
    public void userConnection() {
    	User u = new User("julianh2o@gmail.com","secret");
    	u.save();
    	
    	assertNotNull(User.connect("julianh2o@gmail.com", "secret"));
    	assertNull(User.connect("julianh2o@gmail.com", "boing"));
    	assertNull(User.connect("julianh2o@gmail.com", "zap"));
    }

}
