package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Project extends TimestampModel {
	public String name;
	
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
}
