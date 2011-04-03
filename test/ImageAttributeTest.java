import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class ImageAttributeTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteAll();
    }
    
    @Test
    public void createImage() {
    	DatabaseImage image = new DatabaseImage("/alana/2.jpg");
    	image.save();
    	
    	ImageAttribute attr = new ImageAttribute(image,"boink", "pow");
    	attr.save();
    	
    	image.attributes.add(attr);
    	
    	image.save();
    	
    	DatabaseImage result = DatabaseImage.find("byPath", "/alana/2.jpg").first();
    	assertEquals(1, result.attributes.size());
    }

}
