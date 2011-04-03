package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;

@Entity
public class ImageAttribute extends TimestampModel {
	@Expose
	public String attribute;
	@Expose
	public String value;
	
	@ManyToOne
	public DatabaseImage image;
	
	public ImageAttribute(DatabaseImage image, String attribute, String value) {
		super();
		this.image = image;
		this.attribute = attribute;
		this.value = value;
	}
}
