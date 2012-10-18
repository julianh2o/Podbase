package controllers;

import java.util.Date;
import java.util.List;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import models.Activation;
import models.Directory;
import models.Project;
import models.User;

public class UserController extends ParentController {
	private static String generateTemporaryPassword() {
		return RandomStringUtils.randomAlphanumeric(8);
	}
	
	public static void getAllUsers() {
		List<User> users = User.findAll();
		renderJSON(users);
	}
	
	public static void createUser(String email) {
		User user = new User(email,null);
		user.save();
		
		Activation.generateActivationCode(user, 3);
		Email.newAccount(user);
		
		renderJSON(user);
	}
	
	private static Activation validateActivationCode(String activationCode) {
		Activation activation = Activation.find("byActivationCode", activationCode).first();
		List<Activation> activations = Activation.findAll();
		if (activation == null) {
			error("Activation code not found");
		}
		
		if (activation.expirationDate.before(new Date())) {
			error("Activation expired");
		}
		
		return activation;
	}
	
	public static void doActivate(String activationCode) {
		Activation activation = validateActivationCode(activationCode);
		if (activation == null) return;
		
		User user = activation.user;
		render(user,activation);
	}
	
	public static void completeActivation(User user, String activationCode, String password, String confirm) {
		Activation activation = validateActivationCode(activationCode);
		if (activation == null) return;
		
		validatePassword(password,confirm);
		
		user.setCleartextPassword(password);
		
		user.activation = null;
		user.save();
		
		activation.delete();
		
		//TODO extract this?
		session.put("username", user.email);
		
		Application.index();
	}

	private static void validatePassword(String password, String confirm) {
		if (!password.equals(confirm)) {
			error("Passwords do not match.");
		}
		
		if (password.length() < 5) {
			error("Password is too short. 5 character minimum.");
		}
	}
}
