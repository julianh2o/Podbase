package models;

import java.util.*;

import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

@Entity
public class User extends TimestampModel {
	@Email
	public String email;
	
	public String password;
	
	@OneToOne(optional=true,cascade=CascadeType.ALL)
	public Activation activation;
	
	public boolean root;
	public boolean guest;
	
	public User(String email, String password) {
		super();
		this.root = false;
		this.email = email;
		this.password = null;
		if (password != null) setCleartextPassword(password);
	}
	
	public void setCleartextPassword(String password) {
		this.password = encryptPassword(password); 
	}
	
	public String encryptPassword(String password) {
		return Codec.hexMD5(password);
	}
	
	public boolean authenticate(String password) {
		return encryptPassword(password).equals(this.password);
	}
	
	public static User connect(String email, String password) {
	    return find("byEmailAndPassword", email, password).first();
	}
	
	public String toString() {
		return email + " ["+created+"]";
	}
	
	public boolean isGuest() {
		return this.equals(getGuestAccount());
	}
	
	public static User getGuestAccount() {
		return User.find("email", "guest").first();
	}

	public static User getAuthenticatedAccount() {
		return User.find("authenticated", true).first();
	}
}