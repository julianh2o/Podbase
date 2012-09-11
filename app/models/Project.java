package models;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import models.UserPermission;

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
	
	public ImageSet imageSet;

	public Project(String name) {
		super();
		this.name = name;
		this.imageSet = new ImageSet(name);
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
	
	public Set<String> getUserAccess(User user) {
		List<UserPermission> permissions = UserPermission.find("byProjectAndUser", this, user).fetch();
		HashSet<String> access = new HashSet<String>();
		for (UserPermission perm : permissions) {
			access.add(perm.permission);
		}
		
		for (Entry<String, List<String>> e : UserPermission.implications.entrySet()) {
			if (access.contains(e.getKey())) {
				for (String implied : e.getValue()) {
					access.add(implied);
				}
			}
		}
		
		return access;
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
}
