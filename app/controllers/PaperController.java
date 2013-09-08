// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import play.mvc.With;

import services.PathService;
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

@With(Security.class)
public class PaperController extends ParentController {
	@PaperAccess(AccessType.LISTED)
	public static void render(long imagesetid, int size) {
		ImageSet imageset = ImageSet.findById(imagesetid);
    	List<DatabaseImage> images = ImageSetMembership.getImages(imageset.images);
    	render(images,size);
	}
	
    public static void getPapers() {
    	User user = Security.getUser();
    	
    	if (user.isRoot()) renderJSON(Paper.findAll());
    	
    	Set<Paper> papers = PermissionService.filter(PermissionService.getModelsForUser(user, AccessType.LISTED), Paper.class);
    	
    	renderJSON(papers);
    }
    
	@PaperAccess(AccessType.LISTED)
    public static void getPaper(Paper paper) {
    	renderJSON(paper);
    }
    
	@Access(AccessType.CREATE_PAPER)
    public static void createPaper(String name) {
    	Paper paper = Paper.createPaper(name);
    	
    	User user = Security.getUser();
    	PermissionService.togglePermission(user,paper,AccessType.OWNER,true);
    	
    	ok();
    }
    
	@PaperAccess(AccessType.OWNER)
    public static void deletePaper(Paper paper) {
    	paper.delete();
    	ok();
    }
    
	@PaperAccess(AccessType.LISTED)
    public static void getImageSet(ImageSet imageset) {
    	renderJSON(imageset);
    }
    
	@PaperAccess(AccessType.EDIT_ANALYSIS_METADATA)
    public static void addImageToSet(ImageSet imageset, Path path) {
    	for (ImageSetMembership mem : imageset.images) {
    		DatabaseImage img = mem.image;
    		if (img.getPath().equals(path)) {
    			error("Image already in set");
    			return;
    		}
    	}
    	
    	DatabaseImage image = DatabaseImage.forPath(path);
    	ImageSetMembership.addImageToSet(imageset, image);
    	ok();
    }
    
	@PaperAccess(AccessType.EDIT_ANALYSIS_METADATA)
    public static void removeImageFromSet(ImageSet imageset, Path path) {
    	DatabaseImage image = DatabaseImage.forPath(path);
    	
    	ImageSetMembership mem = ImageSetMembership.find("byImageAndImageset", image, imageset).first();
    	
    	mem.delete();
    	ok();
    }
}
