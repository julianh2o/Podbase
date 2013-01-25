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

import services.PermissionService;
import services.ProjectService;

import controllers.ProjectController;
import controllers.Security;

public class TestPermissionService extends UnitTest {
	@Before
	public void initialize() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		
		User u1 = User.get("test1@test.com");
		Project p1 = Project.get("test");
		
		PermissionService.addPermission(u1,p1,AccessType.LISTED);
	}
	
	public boolean hasPermission(User u, Project p, AccessType type) {
		List<AccessType> perms = PermissionService.getAccess(u, p);
		return perms.contains(type);
	}

	@Test
	public void testAddPermission() {
		User u1 = User.get("test1@test.com");
		Project p1 = Project.get("test");
		
		PermissionService.addPermission(u1,p1,AccessType.VISIBLE);
		
		assertTrue(hasPermission(u1,p1,AccessType.VISIBLE));
	}
	
	@Test
	public void testRemovePermission() {
		User u1 = User.get("test1@test.com");
		Project p1 = Project.get("test");
		
		PermissionService.removePermission(u1,p1,AccessType.LISTED);
		
		assertFalse(hasPermission(u1,p1,AccessType.LISTED));
	}
	
	@Test
	public void testTogglePermission() {
		System.out.println("toggle test");
		User u1 = User.get("test1@test.com");
		Project p1 = Project.get("test");
		
		PermissionService.togglePermission(u1,p1,AccessType.OWNER,true);
		assertTrue(hasPermission(u1,p1,AccessType.OWNER));
		
		PermissionService.togglePermission(u1,p1,AccessType.OWNER,false);
		assertFalse(hasPermission(u1,p1,AccessType.OWNER));
	}
	
}
