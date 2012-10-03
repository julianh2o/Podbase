package controllers;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;

import models.Directory;
import models.Project;
import models.User;
import models.UserPermission;

public class UserController extends ParentController {
	private static String generateTemporaryPassword() {
		return RandomStringUtils.randomAlphanumeric(8);
	}
	
	public static void getAllUsers() {
		List<User> users = User.findAll();
		renderJSON(users);
	}
	
	public static void createUser(String email) {
		String password = generateTemporaryPassword();
		User user = new User(email,password);
		user.save();
		
		renderText(password);
	}
}
