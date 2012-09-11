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
	
	public boolean root;
	public boolean guest;
	
	@OneToMany(mappedBy="user",cascade=CascadeType.ALL)
	@GsonTransient
	public List<UserPermission> userPermissions;
	
	public User(String email, String password) {
		super();
		this.root = false;
		this.email = email;
		this.guest = false;
		setCleartextPassword(password);
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
	
	public List<UserPermission> getPermissionsForProject(Project project) {
		return project.getPermissionsForUser(this);
	}
	
	public List<Project> getProjectsWithPermission(String permission) {
		List<UserPermission> permissions = UserPermission.find("byUserAndPermission", this,permission).fetch();
		List<UserPermission> everyonePermissions = UserPermission.find("byUserAndPermission", null,permission).fetch();
		
		permissions.addAll(everyonePermissions);
		return UserPermission.getProjectList(new LinkedList<UserPermission>(new HashSet<UserPermission>(permissions)));
	}
	
	public static User getGuestAccount() {
		return User.find("guest", true).first();
	}
}