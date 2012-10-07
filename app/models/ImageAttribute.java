package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;

@Entity
public class ImageAttribute extends TimestampModel {
	@ManyToOne
	@GsonTransient
	Project project;
	
	public String attribute;
	public String value;

	public boolean data;

	@ManyToOne
	@GsonTransient
	public DatabaseImage image;

	@Transient
	public boolean templated = false;

	public ImageAttribute(Project project, DatabaseImage image, String attribute, String value, boolean data) {
		this(project, image, attribute, value, data, false);
	}

	public ImageAttribute(Project project, DatabaseImage image, String attribute, String value, boolean data, boolean templated) {
		super();
		this.project = project;
		this.image = image;
		this.attribute = attribute;
		this.value = value;
		this.data = data;
		this.templated = templated;
	}
}
