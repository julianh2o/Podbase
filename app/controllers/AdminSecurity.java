// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import models.User;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * Gate for the Play CRUD admin controllers (CRUD*.java).
 *
 * The CRUD module is a development tool: it gives raw create/read/update/delete
 * access to every entity it is mounted for. It is reachable both through
 * /admin and through the catch-all /{controller}/{action} route, so the check
 * lives on the controllers themselves rather than on a route.
 *
 *  - Disabled unless podbase.admin.enabled=true (responds 404).
 *  - When enabled, only the root account may use it.
 */
public class AdminSecurity extends Controller {
	public static final String ENABLED_PROPERTY = "podbase.admin.enabled";

	public static boolean isAdminEnabled() {
		return "true".equalsIgnoreCase(Play.configuration.getProperty(ENABLED_PROPERTY, "false").trim());
	}

	@Before(priority = 0)
	static void requireRootForAdmin() {
		if (!isAdminEnabled()) notFound();

		User user = Security.getUser();
		if (user == null || user.isGuest()) {
			flash.put("url", "GET".equals(request.method) ? request.url : "/");
			redirect("/login");
		}
		if (!user.isRoot()) forbidden("The admin panel is restricted to the root account.");
	}
}
