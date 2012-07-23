package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;

@Entity
public class ImageAttribute extends TimestampModel {
	public String attribute;
	public String value;
	
	@ManyToOne
	@GsonTransient
	public DatabaseImage image;
	
	@Transient
	public boolean templated = false;
	
	public ImageAttribute(DatabaseImage image, String attribute, String value) {
		this(image,attribute,value,false);
	}
	
	public ImageAttribute(DatabaseImage image, String attribute, String value, boolean templated) {
		super();
		this.image = image;
		this.attribute = attribute;
		this.value = value;
		this.templated = templated;
	}
}
