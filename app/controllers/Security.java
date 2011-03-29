package controllers;

import java.util.List;

import models.User;
 
public class Security extends Secure.Security {
	
    static boolean authentify(String email, String password) {
        User user = User.find("byEmail", email).first();
        return user != null && user.authenticate(password);
    }
    
    static void checkPermission() {
    	String email = connected();
    	User user = User.find("byEmail", email).first();
    	if (user == null) redirect("/login");
    }
}
