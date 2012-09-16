package controllers;

import java.util.List;

import models.DatabaseImage;
import models.Directory;
import models.ImageSet;
import models.ImageSetMembership;
import models.Paper;
import models.Project;
import models.User;
import models.UserPermission;

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
    
    public static void getPaper(long paperId) {
    	Paper paper = Paper.findById(paperId);
    	renderJSON(paper);
    }
    
    public static void createPaper(String name) {
    	Paper paper = Paper.createPaper(name);
    	ok();
    }
    
    public static void deletePaper(long paperId) {
    	Paper paper = Paper.findById(paperId);
    	paper.delete();
    	ok();
    }
    
    //Image Sets
    public static void getImageSet(long imagesetId) {
    	ImageSet set = ImageSet.findById(imagesetId);
    	renderJSON(set);
    }
    
    //TODO access control
    public static void addImageToSet(long imagesetId, String path) {
    	ImageSet set = ImageSet.findById(imagesetId);
    	
    	for (ImageSetMembership mem : set.images) {
    		DatabaseImage img = mem.image;
    		if (img.path.equals(path)) {
    			error("Image already in set");
    			return;
    		}
    	}
    	
    	DatabaseImage image = DatabaseImage.forPath(path);
    	ImageSetMembership.addImageToSet(set, image);
    	ok();
    }
    
    public static void removeImageFromSet(long imagesetId, String path) {
    	DatabaseImage image = DatabaseImage.forPath(path);
    	ImageSet set = ImageSet.findById(imagesetId);
    	
    	ImageSetMembership mem = ImageSetMembership.find("byImageAndImageset", image,set).first();
    	
    	mem.delete();
    	ok();
    }
}
