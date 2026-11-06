// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
import java.util.HashMap;
import java.util.Map;

import models.Activation;
import models.Podbase;
import models.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.db.jpa.JPA;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * Regression tests for the issues in the Podbase security report:
 *  1. directory traversal through Application.loadJavascript
 *  2. unauthenticated Play CRUD admin panel
 *  3. user list leaking activation codes / activation takeover
 */
public class SecurityFunctionalTest extends FunctionalTest {
	private static final String ROOT_PW = "root-test-pw";
	private static final String USER_PW = "user-test-pw";

	private String pendingCode;
	private String attackerCode;

	@Before
	public void setUp() {
		Fixtures.deleteDatabase();
		new Podbase().save();
		User root = new User("root", ROOT_PW); root.special = true; root.save();
		User guest = new User("guest", null); guest.special = true; guest.save();
		User auth = new User("authenticated", null); auth.special = true; auth.save();
		new User("alice@example.com", USER_PW).save();

		User pending = new User("pending@example.com", null).save();
		pendingCode = Activation.generateActivationCode(pending, 30).activationCode;

		User attacker = new User("attacker@example.com", null).save();
		attackerCode = Activation.generateActivationCode(attacker, 30).activationCode;

		// Functional requests run in their own transaction; make the fixtures visible.
		JPA.em().getTransaction().commit();
		JPA.em().getTransaction().begin();
		clearCookies();
		Play.configuration.setProperty("podbase.admin.enabled", "true");
	}

	@After
	public void tearDown() {
		Play.configuration.setProperty("podbase.admin.enabled", "true");
	}

	private void login(String user, String pw) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", user);
		params.put("password", pw);
		Response r = POST("/login", params);
		assertStatus(302, r);
		assertFalse("login failed for " + user, r.getHeader("Location").contains("login"));
	}

	// ---- Issue 1: directory traversal ----

	@Test
	public void javascriptLoaderServesRealScriptsAndTemplates() {
		assertIsOk(GET("/public/javascripts/index.js"));
		assertIsOk(GET("/public/javascripts/tmpl/Header.html"));
		assertIsOk(GET("/application/loadjavascript?path=index.js"));
	}

	@Test
	public void javascriptLoaderRejectsTraversal() {
		String[] bad = {
			"../../conf/application.conf",
			"../../conf/routes",
			"..%2F..%2Fconf%2Froutes",
			"view/../../../conf/routes",
			"/etc/passwd",
			"../../../../../../etc/passwd",
			"..\\..\\conf\\routes",
			"index.js%00.html",
			"../../app/controllers/Application.java",
			"",
		};
		for (String p : bad) {
			Response r = GET("/application/loadjavascript?path=" + p);
			assertEquals("path=" + p, (Object) 404, r.status);
		}
		assertStatus(404, GET("/application/loadjavascript"));
		assertStatus(404, GET("/public/javascripts/..%2F..%2Fconf%2Froutes"));
	}

	// ---- Issue 2: CRUD admin ----

	@Test
	public void adminRequiresLogin() {
		Response r = GET("/admin/");
		assertStatus(302, r);
		assertTrue(r.getHeader("Location").contains("/login"));
		assertStatus(302, GET("/crudactivation/list"));
		assertStatus(302, GET("/crudpermission/list"));
		assertStatus(302, GET("/cruddatabaseimage/list"));
	}

	@Test
	public void adminForbidsNonRoot() {
		login("alice@example.com", USER_PW);
		assertStatus(403, GET("/admin/"));
		assertStatus(403, GET("/admin/crudactivation"));
		assertStatus(403, GET("/crudactivation/list"));
		assertStatus(403, GET("/crudpermission/list"));
	}

	@Test
	public void adminWorksForRoot() {
		login("root", ROOT_PW);
		assertIsOk(GET("/admin/crudactivation"));
	}

	@Test
	public void adminDisabledByConfigIs404EvenForRoot() {
		Play.configuration.setProperty("podbase.admin.enabled", "false");
		login("root", ROOT_PW);
		assertStatus(404, GET("/admin/crudactivation"));
		assertStatus(404, GET("/crudactivation/list"));
	}

	// ---- Issue 3: user list / activation takeover ----

	@Test
	public void getAllUsersRequiresLogin() {
		Response r = GET("/usercontroller/getallusers");
		assertFalse("guest got the user list", r.status == 200);
		assertFalse(getContent(r).contains(pendingCode));
	}

	@Test
	public void getAllUsersHidesActivationCodesFromNonRoot() {
		login("alice@example.com", USER_PW);
		Response r = GET("/usercontroller/getallusers");
		assertIsOk(r);
		String body = getContent(r);
		assertTrue(body.contains("pending@example.com"));
		assertFalse(body.contains(pendingCode));
		assertFalse(body.contains("activationCode"));
	}

	@Test
	public void getAllUsersShowsActivationCodesToRoot() {
		login("root", ROOT_PW);
		Response r = GET("/usercontroller/getallusers");
		assertIsOk(r);
		assertTrue(getContent(r).contains(pendingCode));
	}

	@Test
	public void activationCodeCannotBeRedirectedToAnotherAccount() {
		User root = User.get("root");
		Map<String, String> params = new HashMap<String, String>();
		params.put("user.id", String.valueOf(root.id));
		params.put("activationCode", attackerCode);
		params.put("password", "pwned123");
		params.put("confirm", "pwned123");
		POST("/usercontroller/completeactivation", params);

		clearCookies();
		Map<String, String> login = new HashMap<String, String>();
		login.put("username", "root");
		login.put("password", "pwned123");
		Response r = POST("/login", login);
		assertTrue("root password was changed by an attacker's activation code",
				r.getHeader("Location") == null || r.getHeader("Location").contains("login"));

		// The code still activated the account it was issued for.
		clearCookies();
		login("attacker@example.com", "pwned123");
	}

	@Test
	public void resendActivationRequiresPermission() {
		Response r = GET("/usercontroller/resendactivation?email=pending@example.com");
		assertFalse(r.status == 200);
	}

	@Test
	public void securityHeadersPresent() {
		Response r = GET("/");
		assertEquals("nosniff", r.getHeader("X-Content-Type-Options"));
		assertEquals("SAMEORIGIN", r.getHeader("X-Frame-Options"));
	}
}
