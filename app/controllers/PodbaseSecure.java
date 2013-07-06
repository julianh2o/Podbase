package controllers;

import play.data.validation.Required;
import play.libs.Crypto;

public class PodbaseSecure extends ParentController {
    public static void authenticate(@Required String username, String password, boolean remember, String redirect) throws Throwable {
        Boolean allowed = false;
        allowed = Security.authenticate(username, password);
        
        if(validation.hasErrors() || !allowed) {
        	String redirectUrl = flash.get("url");
        	flash.put("url", redirectUrl);
            
            flash.error("secure.error");
            params.flash();
            Secure.login();
        }
        
        session.put("username", username);
        
        if(remember) {
            response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "30d");
        }
        
        redirect(redirect == null ? "/" : redirect);
    }
}
