package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.modules.search.Field;
import play.modules.search.Indexed;

import com.google.gson.annotations.Expose;

@Entity
@Indexed
public class ImageAttribute extends TimestampModel {
	@ManyToOne
	@GsonTransient
	Project project;
	
	public String attribute;
	@Field
	public String value;
	public boolean hidden;

	public boolean data;

	@ManyToOne
	@GsonTransient
	public DatabaseImage image;

	@Transient
	public boolean templated = false;

	public ImageAttribute(Project project, DatabaseImage image, String attribute, String value, boolean data) {
		this(project, image, attribute, value, data, false, false);
	}

	public ImageAttribute(Project project, DatabaseImage image, String attribute, String value, boolean data, boolean templated, boolean hidden) {
		super();
		this.project = project;
		this.image = image;
		this.attribute = attribute;
		this.value = value;
		this.data = data;
		this.templated = templated;
		this.hidden = hidden;
	}
}
