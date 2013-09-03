// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import play.data.validation.Required;
import play.libs.Crypto;

public class PodbaseSecure extends ParentController {
    public static void authenticate(@Required String username, String password, String hash, boolean remember) throws Throwable {
        Boolean allowed = false;
        allowed = Security.authenticate(username, password);
        
    	String redirectUrl = flash.get("url");
        
        if(validation.hasErrors() || !allowed) {
        	flash.put("url", redirectUrl);
            
            flash.error("secure.error");
            params.flash();
            Secure.login();
        }
        
        session.put("username", username);
        
        if(remember) {
            response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "30d");
        }
        
        if (redirectUrl == null) redirectUrl = "/";
        
        if (hash != null) redirectUrl += hash;
        
        redirect(redirectUrl);
    }
}
