// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import play.modules.search.Field;
import play.modules.search.Indexed;
import play.modules.search.Search;
import play.modules.search.store.FilesystemStore;

import com.google.gson.annotations.Expose;

@Entity
@Indexed
public class ImageAttribute extends TimestampModel {
	@ManyToOne
	@GsonTransient
	@Field
	public Project project;
	
	@Column(columnDefinition="char(60)")
	public String attribute;
	
	@Field
	@Column(columnDefinition="char(255)")
	public String value;
	
	public int ordering;
	
	public boolean data;
	
	public boolean hidden;

	@ManyToOne
	@GsonTransient
	public DatabaseImage image;
	
	@OneToOne
	@GsonTransient
	public ImageAttribute linkedAttribute;

	@Transient
	public boolean templated = false;

	public ImageAttribute(Project project, DatabaseImage image, String attribute, String value, boolean data, int ordering) {
		this(project, image, attribute, value, data, ordering, false, false);
	}

	public ImageAttribute(Project project, DatabaseImage image, String attribute, String value, boolean data, int ordering, boolean templated, boolean hidden) {
		super();
		((FilesystemStore)Search.getCurrentStore()).sync = false;
		this.project = project;
		this.image = image;
		this.attribute = attribute;
		this.value = value;
		this.data = data;
		this.ordering = ordering;
		this.templated = templated;
		this.hidden = hidden;
	}
}
