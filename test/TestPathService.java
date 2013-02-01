import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import models.Permission;
import models.Project;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

import static org.junit.Assert.*;

import access.AccessType;

import services.PathService;
import services.PermissionService;
import services.ProjectService;

import controllers.ProjectController;
import controllers.Security;

public class TestPathService extends UnitTest {
	@Before
	public void initialize() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		
		User u1 = User.get("test1@test.com");
		Project p1 = Project.get("test");
		
		PermissionService.addPermission(u1,p1,AccessType.LISTED);
	}
	
	
	@Test
	public void testPathValidation() {
		PathService.assertPath("/");
		PathService.assertPath("/foo");
		PathService.assertPath("/bar/baz");
		PathService.assertPath("/ziggle/meep");
	}
	
	@Test(expected=RuntimeException.class)
	public void testPathValidation2() {
		PathService.assertPath("/ziggle/meep/");
	}
	
	@Test(expected=RuntimeException.class)
	public void testPathValidation3() {
		PathService.assertPath("//");
	}
	
	@Test(expected=RuntimeException.class)
	public void testPathValidation4() {
		PathService.assertPath("/..");
	}
	
	@Test(expected=RuntimeException.class)
	public void testPathValidation5() {
		PathService.assertPath("/../..");
	}
	
	@Test(expected=RuntimeException.class)
	public void testPathValidation6() {
		PathService.assertPath("foo");
	}
	
	@Test
	public void testIsImage() {
		assertTrue(PathService.isImage(Paths.get("foo.jpg")));
		assertTrue(PathService.isImage(Paths.get("foo.png")));
		assertTrue(PathService.isImage(Paths.get("foo.tiff")));
		assertTrue(PathService.isImage(Paths.get("foo.tif")));
		assertTrue(PathService.isImage(Paths.get("foo.gif")));
		assertTrue(PathService.isImage(Paths.get("foo.jpeg")));
		assertTrue(PathService.isImage(Paths.get("foo.gif")));
		
		assertFalse(PathService.isImage(Paths.get("foo.xml")));
		assertFalse(PathService.isImage(Paths.get("foo.yml")));
		assertFalse(PathService.isImage(Paths.get("foo.txt")));
	}
}
