package controllers;

import java.util.List;

import models.DatabaseImage;
import models.Directory;
import models.ImageSet;
import models.ImageSetMembership;
import models.Paper;
import models.Project;
import models.User;

public class PaperController extends ParentController {
	public static void render(long imagesetId, int size) {
    	ImageSet imageset = ImageSet.findById(imagesetId);
    	List<DatabaseImage> images = ImageSetMembership.getImages(imageset.images);
    	render(images,size);
	}
	
    public static void getPapers() {
    	//TODO access control
    	List<Paper> papers = Paper.findAll();
    	renderJSON(papers);
    }
    
    public static void getPaper(Paper paper) {
    	renderJSON(paper);
    }
    
    public static void createPaper(String name) {
    	Paper paper = Paper.createPaper(name);
    	ok();
    }
    
    public static void deletePaper(Paper paper) {
    	paper.delete();
    	ok();
    }
    
    //Image Sets
    public static void getImageSet(ImageSet imageset) {
    	renderJSON(imageset);
    }
    
    //TODO access control
    public static void addImageToSet(ImageSet imageset, String path) {
    	for (ImageSetMembership mem : imageset.images) {
    		DatabaseImage img = mem.image;
    		if (img.path.equals(path)) {
    			error("Image already in set");
    			return;
    		}
    	}
    	
    	DatabaseImage image = DatabaseImage.forPath(path);
    	ImageSetMembership.addImageToSet(imageset, image);
    	ok();
    }
    
    public static void removeImageFromSet(ImageSet imageset, String path) {
    	DatabaseImage image = DatabaseImage.forPath(path);
    	
    	ImageSetMembership mem = ImageSetMembership.find("byImageAndImageset", image, imageset).first();
    	
    	mem.delete();
    	ok();
    }
}
