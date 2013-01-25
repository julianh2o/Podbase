package models;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Project extends PermissionedModel {
	public String name;
	
	public boolean dataMode;
	
	@OneToMany(mappedBy="project",cascade=CascadeType.ALL)
	public List<Directory> directories = new LinkedList<Directory>();
	
	@OneToMany(mappedBy="project", cascade=CascadeType.ALL)
	public List<Template> templates = new LinkedList<Template>();
	
	public static Project get(String name) {
		return (Project)Project.find("byName", name).first();
	}
	
	public Project(String name) {
		super();
		this.dataMode = true;
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
}
