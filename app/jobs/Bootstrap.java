// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package jobs;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import controllers.ImageBrowser;

import access.AccessType;

import play.*;
import play.jobs.*;
import play.test.*;
import services.PathService;
import services.PermissionService;
 
import models.*;
 
@OnApplicationStart
public class Bootstrap extends Job {
	private final String DEFAULT_ROOT_PASSWORD = "password";
	
    public void doJob() {
		if (!(Play.id.equals("dev") || Play.id.equals("setup"))) {
			System.out.println("Skipping Bootstrap!");
			return;
		}
		
		//System.out.println("Clearing database and bootstrapping.");
        //Fixtures.deleteDatabase();
		
		if (Podbase.findAll().size() > 0) {
			System.out.println("Podbase is already bootstrapped.");
			return;
		}
        
		System.out.println("Bootstrapping database!");
        new Podbase().save();
        
        String rootPassword = Play.configuration.getProperty("podbase.root.password",DEFAULT_ROOT_PASSWORD);
        if (rootPassword.equals(DEFAULT_ROOT_PASSWORD)) System.out.println("Warning: Using default root password!");
        User root = new User("root",rootPassword);
        root.special = true;
        root.save();
        
        User guest = new User("guest",null);
        guest.special = true;
        guest.save();
        
        User authenticated = new User("authenticated",null);
        authenticated.special = true;
        authenticated.save();
    }
}
