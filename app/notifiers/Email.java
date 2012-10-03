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
