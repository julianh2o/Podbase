package models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Project extends TimestampModel {
	public String name;
	
	@OneToMany(mappedBy="project",cascade=CascadeType.ALL)
	@GsonTransient
	public List<UserPermission> userPermissions;
	
	@OneToMany(mappedBy="project",cascade=CascadeType.ALL)
	public List<Directory> directories;
	
	@OneToMany(mappedBy="project", cascade=CascadeType.ALL)
	public List<Template> templates;

	public Project(String name) {
		super();
		this.name = name;
	}
	
	public Directory addDirectory(String path) {
		Directory dir = new Directory(this,path).save();
		this.directories.add(dir);
		this.save();
		return dir;
	}
	
	public Template addTemplate(String name) {
		Template template = new Template(this, name).save();
		this.templates.add(template);
		this.save();
		return template;
	}
	
	public List<UserPermission> getPermissionsForUser(User user) {
		List<UserPermission> permissions = UserPermission.find("byProjectAndUser", this, user).fetch();
		return permissions;
	}
	
	public List<User> getUsersWithPermission(String permission) {
		List<UserPermission> permissions = getUserPermissionsForPermission(permission);
		return UserPermission.getUserList(permissions);
	}
	
	public List<UserPermission> getUserPermissionsForPermission(String permission) {
		List<UserPermission> permissions = UserPermission.find("byProjectAndPermission", this, permission).fetch();
		return permissions;
	}
	
	public List<User> getUsers() {
		return getUsersWithPermission("listed");
	}
	
	public static List<UserPermission> getProjectsWithGuestPermission(String permission) {
		List<UserPermission> permissions = UserPermission.find("byUserAndPermission", null, permission).fetch();
		return permissions;
	}
}
