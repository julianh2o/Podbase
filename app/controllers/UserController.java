// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import access.Access;
import access.AccessType;
import play.Play;
import play.mvc.Util;
import play.mvc.With;
import services.PermissionService;
import util.PodbaseUtil;
import models.Activation;
import models.Directory;
import models.Project;
import models.User;

@With(Security.class)
public class UserController extends ParentController {
	private static String generateTemporaryPassword() {
		return RandomStringUtils.random(8, 0, 0, true, true, null, new SecureRandom());
	}
	
	/**
	 * Lists all users. Requires a logged-in (non-guest) account. Activation codes
	 * are only included for root: anyone holding a code can set that account's
	 * password, so they must never be handed to other users.
	 */
	public static void getAllUsers() {
		User current = Security.getUser();
		if (current == null || current.isGuest()) forbidden("You must be logged in to list users.");

		List<User> users = User.findAll();
		if (current.isRoot()) {
			renderJSON(PodbaseUtil.getGsonIncludingActivationCodes().toJson(users));
		}
		renderJSON(users);
	}
	
	public static void changePassword(String oldpassword, String newpassword) {
		User u = Security.getUser();
		if (!u.authenticate(oldpassword)) {
			error("Incorrect password!");
		}
		
		validatePassword(newpassword,newpassword);
			
		u.setCleartextPassword(newpassword);
		u.save();
		jsonOk();
	}
	
	@Access(AccessType.CREATE_USERS)
	public static void createUser(String email) {
		if (email.contains("\0")) forbidden();
		
		User user = new User(email,null);
		user.save();
		
		Activation.generateActivationCode(user, 30);
		Email.newAccount(Security.getUser(),user);
		
		renderJSON(user);
	}
	
	@Access(AccessType.DELETE_USERS)
	public static void deleteUser(User user) {
		if (user.special) forbidden("You cannot delete special users!");
		
		user.delete();
		jsonOk();
	}
	
	@Access(AccessType.CREATE_USERS)
	public static void resendActivation(String email) {
		User user = User.find("byEmail", email).first();
		if (user == null || user.special) notFound();
		Activation.generateActivationCode(user, 30);
		Email.newAccount(Security.getUser(),user);
		
		renderText("Your activation code has been re-sent to: "+email);
	}
	
	private static Activation validateActivationCode(String activationCode) {
		if (activationCode == null || activationCode.isEmpty()) error("Activation code not found");
		Activation activation = Activation.find("byActivationCode", activationCode).first();
		//List<Activation> activations = Activation.findAll();
		if (activation == null) {
			error("Activation code not found");
		}
		
		if (activation.expirationDate.before(new Date())) {
			error("Activation expired - resending activation");
			String email = activation.user.email;
			activation.delete();
			resendActivation(email);
		}
		
		return activation;
	}
	
	public static void doActivate(String activationCode) {
		Activation activation = validateActivationCode(activationCode);
		if (activation == null) return;
		
		User user = activation.user;
		render(user,activation);
	}
	
	public static void completeActivation(String activationCode, String password, String confirm) {
		Activation activation = validateActivationCode(activationCode);
		if (activation == null) return;
		
		// The account is the one the activation code was issued to. Never trust a
		// user id from the request: that let any valid code reset any account.
		User user = activation.user;
		if (user == null || user.special) forbidden();
		
		validatePassword(password,confirm);
		
		user.setCleartextPassword(password);
		
		user.activation = null;
		user.save();
		
		activation.user = null;
		activation.save();
		
		activation.delete();
		
		session.put("username", user.email);
		
		Application.index();
	}
	
	public static void mimicUser(User user) {
		if (PermissionService.hasInheritedAccess(Security.getUser(), null, AccessType.MIMIC_USERS) || Play.mode.isDev()) {
			session.put("username", user.email);
			ok();
		} else {
			forbidden();
		}
	}

	@Util
	private static void validatePassword(String password, String confirm) {
		if (password == null || !password.equals(confirm)) {
			error("Passwords do not match.");
		}
		
		if (password.length() < 5) {
			error("Password is too short. 5 character minimum.");
		}
	}
}
