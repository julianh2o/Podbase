package controllers;

import java.util.List;

import play.mvc.Util;

import models.Project;
import models.User;
import models.UserPermission;
 
public class Security extends Secure.Security {
    @Util
    public static boolean authentify(String email, String password) {
        User user = User.find("byEmail", email).first();
        return user != null && user.authenticate(password);
    }
    
    @Util
    public static void checkPermission() {
    	String email = connected();
    	User user = User.find("byEmail", email).first();
    	if (user == null) redirect("/login");
    }
    
    @Util
    public static User getUser() {
    	User user = null;
    	if (Security.connected() != null) user = User.find("email", Security.connected()).first();
    	
    	if (user == null) return User.getGuestAccount();
    	return user;
    }

    @Util
	public static User logUserIn(User user) {
		session.put("username", user.email);		
		return user;
	}
}
