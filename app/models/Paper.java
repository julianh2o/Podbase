// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
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

import play.db.jpa.Model;

@Entity
public class Paper extends PermissionedModel {
	public String name;
	
	@OneToOne
	public ImageSet imageset;
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL)
	@GsonTransient
	public List<Permission> permissions = new LinkedList<Permission>();
	
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
	
	public String toString() {
		return this.name;
	}
}
