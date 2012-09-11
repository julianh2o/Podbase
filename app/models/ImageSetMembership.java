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

import models.UserPermission;

import play.db.jpa.Model;

@Entity
public class ImageSetMembership extends TimestampModel {
	@OneToOne
	@GsonTransient
	public ImageSet imageset;

	@OneToOne
	public DatabaseImage image;

	public ImageSetMembership(ImageSet imageset, DatabaseImage image) {
		this.imageset = imageset;
		this.image = image;
	}
	
	public static ImageSetMembership addImageToSet(ImageSet imageset, DatabaseImage image) {
		ImageSetMembership membership = new ImageSetMembership(imageset,image);
		imageset.images.add(membership);
		imageset.save();
		
		return membership;
	}
	
	public String toString() {
		return "Imageset ["+imageset.id+"] contains image ["+image.id+"]";
	}
}
