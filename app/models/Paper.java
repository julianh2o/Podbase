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
import javax.persistence.OneToOne;

import models.UserPermission;

import play.db.jpa.Model;

@Entity
public class Paper extends TimestampModel {
	public String name;
	
	@OneToOne
	public ImageSet imageset;
	
	public Paper(String name) {
		super();
		this.name = name;
		this.imageset = new ImageSet(name);
	}
	
	public static Paper createPaper(String name) {
		Paper paper = new Paper(name);
		paper.imageset.save();
		paper.save();
		return paper;
	}
}
