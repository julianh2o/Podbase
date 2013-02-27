import java.util.Collection;
import java.util.List;
import java.util.Set;

import models.Project;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

import static org.junit.Assert.*;

import access.AccessType;

import services.PermissionService;
import services.ProjectService;

import controllers.ProjectController;
import controllers.Security;

public class TestProjectService extends UnitTest {
	@Before
	public void initialize() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		
		User u1 = User.get("test1@test.com");
		User u2 = User.get("test2@test.com");
		Project p1 = Project.get("test");
		Project p2 = Project.get("rawr");
		
    	PermissionService.togglePermission(u1,p1,AccessType.VISIBLE,true);
    	
    	PermissionService.togglePermission(u2,p1,AccessType.VISIBLE,true);
    	PermissionService.togglePermission(u2,p2,AccessType.VISIBLE,true);
	}

	@Test
	public void testVisibleProjects() {
		User u1 = User.get("test1@test.com");
		Security.logUserIn(u1);
		
		Collection<Project> projects = ProjectService.getVisibleProjects();
		assertTrue(projects.contains(Project.get("test")));
		assertFalse(projects.contains(Project.get("rawr")));
	}
	
	@Test
	public void testVisibleProjects2() {
		User u2 = User.get("test2@test.com");
		Security.logUserIn(u2);
		
		Collection<Project> projects = ProjectService.getVisibleProjects();
		assertTrue(projects.contains(Project.get("test")));
		assertTrue(projects.contains(Project.get("rawr")));
	}
}
