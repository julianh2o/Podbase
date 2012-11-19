package controllers;

import java.util.List;

import services.PermissionService;

import access.Access;
import access.AccessType;
import access.PaperAccess;

import models.DatabaseImage;
import models.Directory;
import models.ImageSet;
import models.ImageSetMembership;
import models.Paper;
import models.Project;
import models.User;

public class PaperController extends ParentController {
	@PaperAccess(AccessType.VISIBLE)
	public static void render(long imagesetid, int size) {
		ImageSet imageset = ImageSet.findById(imagesetid);
    	List<DatabaseImage> images = ImageSetMembership.getImages(imageset.images);
    	render(images,size);
	}
	
    public static void getPapers() {
    	User user = Security.getUser();
    	
    	if (user.root) renderJSON(Paper.findAll());
    	
    	List<Paper> papers = PermissionService.filter(PermissionService.getModelsForUser(user, AccessType.VISIBLE), Paper.class);
    	
    	renderJSON(papers);
    }
    
	@PaperAccess(AccessType.VISIBLE)
    public static void getPaper(Paper paper) {
    	renderJSON(paper);
    }
    
	@Access(AccessType.CREATE_PAPER)
    public static void createPaper(String name) {
    	Paper paper = Paper.createPaper(name);
    	
    	User user = Security.getUser();
    	PermissionService.togglePermission(user,paper,AccessType.OWNER,true);
    	PermissionService.togglePermission(user,paper,AccessType.VISIBLE,true);
    	PermissionService.togglePermission(user,paper,AccessType.LISTED,true);
    	PermissionService.togglePermission(user,paper,AccessType.MANAGE_PERMISSIONS,true);
    	
    	ok();
    }
    
	@PaperAccess(AccessType.OWNER)
    public static void deletePaper(Paper paper) {
    	paper.delete();
    	ok();
    }
    
	@PaperAccess(AccessType.EDITOR)
    public static void getImageSet(ImageSet imageset) {
    	renderJSON(imageset);
    }
    
	@PaperAccess(AccessType.EDITOR)
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
    
	@PaperAccess(AccessType.EDITOR)
    public static void removeImageFromSet(ImageSet imageset, String path) {
    	DatabaseImage image = DatabaseImage.forPath(path);
    	
    	ImageSetMembership mem = ImageSetMembership.find("byImageAndImageset", image, imageset).first();
    	
    	mem.delete();
    	ok();
    }
}
