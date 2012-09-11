package models;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.UniqueConstraint;

@Entity
public class DatabaseImage extends TimestampModel {
	public String path;
	
	@OneToMany(mappedBy="image", cascade=CascadeType.ALL)
	public List<ImageAttribute> attributes;

	public DatabaseImage(String path) {
		super();
		this.path = path;
		this.attributes = new LinkedList<ImageAttribute>();
	}
	
	public ImageAttribute addAttribute(String key, String value) {
		ImageAttribute attr = new ImageAttribute(this, key, value).save();
		this.attributes.add(attr);
		this.save();
		return attr;
	}
	
	public static DatabaseImage forPath(String path) {
		if (path == null) return null;
		DatabaseImage image = DatabaseImage.find("path",path).first();
		if (image == null) {
			image = new DatabaseImage(path);
			image.save();
		}
		return image;
	}
}
