// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.UniqueConstraint;

import play.modules.search.Field;
import play.modules.search.Indexed;
import services.PathService;
import util.PodbaseUtil;

@Entity
@Indexed
public class DatabaseImage extends TimestampModel {
	@Field
	private String path;
	public String hash;
	
	@OneToMany(mappedBy="image", cascade=CascadeType.ALL)
	@OrderBy("ordering")
	public List<ImageAttribute> attributes;
	
	public boolean imported;

	public DatabaseImage(String path) {
		super();
		
		PathService.assertPath(path);
		
		this.path = path;
		Path pathObject = PathService.resolve(path);
		
		this.hash = null;
		try {
			if (!pathObject.toFile().isDirectory()) this.hash = PathService.calculateImageHash(pathObject);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.attributes = new LinkedList<ImageAttribute>();
		this.imported = false;
	}
	
	public ImageAttribute addAttribute(Project project, String key, String value, boolean dataMode) {
		return this.addAttribute(project,key,value,dataMode,null);
	}
	
	public ImageAttribute addAttribute(Project project, String key, String value, boolean dataMode, Integer ordering) {
		if (ordering == null) ordering = this.attributes.size();
		ImageAttribute attr = new ImageAttribute(project, this, key, value, dataMode, ordering).save();
		return attr;
	}
	
	public Path getPath() {
		return PathService.resolve(this.path);
	}
	
	public String getStringPath() {
		return this.path;
	}
	
	public static DatabaseImage forPath(Path path) {
		if (path == null) return null;
		
		String rel = PathService.getRelativeString(path);
		DatabaseImage image = DatabaseImage.find("path",rel).first();
		
		if (image != null) {
			if (image.hash == null) {
				System.err.println("Image hash is null: "+rel);
				try {
					image.hash = PathService.calculateImageHash(path);
					image.save();
				} catch (IOException e1) {
					
				}
			}
			return image;
		}
		
		try {
			String imageHash = PathService.calculateImageHash(path);
			image = DatabaseImage.find("hash",imageHash).first();
			if (image != null) {
				String oldPath = image.path;
				image.path = rel;
				image.save();
				Path newPath = PathService.resolve(rel);
				Project p = PathService.projectForPath(newPath);
				Project oldProject = PathService.projectForPath(PathService.resolve(oldPath));
				for (ImageAttribute attr : image.attributes) {
					if (attr.project.id == oldProject.id) {
						//System.out.println("attribute: "+attr.attribute+"="+attr.value+" migrating from "+oldProject.name+" to "+p.name+" (image: "+attr.image.id+")");
						attr.project = p;
						attr.save();
					}
				}
				return image;
			}
		} catch (IOException e) {
			//e.printStackTrace();
			//probably a directory..
			return null;
		}
		
		image = new DatabaseImage(rel);
		image.save();
		return image;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DatabaseImage)) return false;
		DatabaseImage img = (DatabaseImage)o;
		return img.path.equals(this.path);
	}
}
