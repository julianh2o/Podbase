// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package notifiers;

import models.User;
import play.mvc.Mailer;

public class Email extends Mailer {
	public static void newAccount(User user) {
		setSubject("Podbase Account Creation");
		addRecipient(user.email);
		setFrom("Podbase <robot@podbase.net>");
		send(user);
	}
}
