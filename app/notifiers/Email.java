// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package notifiers;

import models.User;
import play.mvc.Mailer;

public class Email extends Mailer {
	public static void newAccount(User sender, User user) {
		setSubject("Podbase.net: User Account Creation");
		System.out.println("user: "+user);
		System.out.println("sender: "+sender);
		addRecipient(user.email);
		setFrom("Podbase.net <admin@podbase.net>");
		if (!sender.special) addBcc(sender.email);
		send(user,sender);
	}
}
