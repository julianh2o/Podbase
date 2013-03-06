package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import play.modules.elasticsearch.annotations.ElasticSearchIgnore;
import play.modules.elasticsearch.annotations.ElasticSearchable;

import com.google.gson.annotations.Expose;

@ElasticSearchable
@Entity
public class ImageAttribute extends TimestampModel {
	@ManyToOne
	@GsonTransient
	@ElasticSearchIgnore
	Project project;
	
	public String attribute;
	
	public String value;
	public boolean hidden;

	public boolean data;

	@ManyToOne
	@GsonTransient
	@ElasticSearchIgnore
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
