package models;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.UniqueConstraint;

import services.PathService;
import util.PodbaseUtil;

@Entity
public class DatabaseImage extends TimestampModel {
	public String path;
	
	@OneToMany(mappedBy="image", cascade=CascadeType.ALL)
	public List<ImageAttribute> attributes;
	
	public boolean imported;

	public DatabaseImage(String path) {
		super();
		
		PathService.assertPath(path);
		
		this.path = path;
		this.attributes = new LinkedList<ImageAttribute>();
		this.imported = false;
	}
	
	public ImageAttribute addAttribute(Project project, String key, String value, boolean dataMode) {
		ImageAttribute attr = new ImageAttribute(project, this, key, value, dataMode).save();
		this.attributes.add(attr);
		this.save();
		return attr;
	}
	
	public Path getPath() {
		return PathService.resolve(this.path);
	}
	
	public static DatabaseImage forPath(Path path) {
		if (path == null) return null;
		
		String rel = PathService.getRelativeString(path);
		DatabaseImage image = DatabaseImage.find("path",rel).first();
		if (image == null) {
			image = new DatabaseImage(rel);
			image.save();
		}
		return image;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DatabaseImage)) return false;
		DatabaseImage img = (DatabaseImage)o;
		return img.path.equals(this.path);
	}
}
