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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class ImageSet extends TimestampModel {
	@OneToOne
	public Paper paper;

	public String name;

	@OneToMany(mappedBy="imageset", cascade=CascadeType.ALL)
	public List<ImageSetMembership> images;

	public ImageSet(String name) {
		this.name = name;
		this.images = new LinkedList();
	}
}
