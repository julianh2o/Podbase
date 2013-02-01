package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Response;

import groovy.lang.DeprecationException;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import access.Access;
import access.AccessType;
import access.ProjectAccess;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.*;
import services.ImageService;
import services.ImportExportService;
import services.PathService;
import services.PermissionService;
import util.*;

@With(Security.class)
public class ImageBrowser extends ParentController {
	public static void index() {
		render();
	}
	
	@ProjectAccess(AccessType.VISIBLE)
	public static void fetchProjectPath(Project project, String strPath) throws FileNotFoundException {
		Path path = PathService.resolve(strPath);
		
		List<Path> paths = PathService.listPaths(path);
		
		paths = PathService.filterImagesAndDirectories(paths);
		
		List<FileWrapper> wrappedFiles = FileWrapper.wrapFiles(project, paths);
		wrappedFiles = FileWrapper.visibilityFilter(project, Security.getUser(), wrappedFiles);
		renderJSON(wrappedFiles);
	}
	
	@Access(AccessType.ROOT)
	public static void fetchPath(String strPath) throws FileNotFoundException {
		throw new DeprecationException("fetchPath is deprecated");
	}
	
	@ProjectAccess(AccessType.EDITOR)
	public static void setVisible(Project project, DatabaseImage image, boolean visible) {
		ProjectVisibleImage.setVisible(project,image,visible);
	}
	
	@Util
	public static BufferedImage getImage(Path path) {
		User user = Security.getUser();
		
		boolean hasAccess = PermissionService.userCanAccessPath(user,path);
		if (!hasAccess) forbidden();

		try {
			return ImageIO.read(path.toFile());
		} catch (IOException e) {
			return null;
		}
	}
	
	public static void resolveFile(String strPath, String mode, Project project, Float scale, Integer width, Integer height, Float brightness, Float contrast, Boolean histogram) throws IOException {
		Path path = PathService.resolve(strPath);
		
		BufferedImage image = getImage(path);
		
		if ("thumb".equals(mode)) {
			image = ImageService.scaleImageToFit(image,200,200);
		} else if ("fit".equals(mode) && width != null && height != null) {
			image = ImageService.scaleImageToFit(image,width,height);
		} else if (width != null && height != null) {
			image = ImageService.scaleImage(image,width,height);
		} else if (width != null || height != null) {
			image = ImageService.scaleImageToFit(image,width,height);
		} else if (scale != null) {
			image = ImageService.scaleImage(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		}
		
		if (histogram == null) histogram = false;
		if (brightness == null) brightness = (float)0;
		if (contrast == null) contrast = (float)1;
		
		image = ImageService.adjustImage(image, brightness, contrast);
		
		if (histogram) {
			BufferedImage hist = ImageService.makeHistogram(image, image.getWidth(), image.getWidth()/2);
			
			image = ImageService.appendImages(image,hist);
		}
		
		//TODO cache this
		renderImage(image);
	}
	
	public static void fetchInfo(Project project, String strPath, boolean dataMode) {
		Path path = PathService.resolve(strPath);
		
		//TODO fix this by using inherited template assignments
		TemplateAssignment assignment = TemplateAssignment.forPath(project,path.getParent());
		
		Template template = assignment==null?null:assignment.template;
		List<TemplateAttribute> templateAttributes = template==null?null:template.attributes;
		
		DatabaseImage image = DatabaseImage.find("path", path).first();
		List<ImageAttribute> attributes = new LinkedList<ImageAttribute>();
		
		if (image != null) attributes.addAll(image.attributes);
		
		//Add attributes from the template
		if (templateAttributes != null) {
			List<ImageAttribute> templateImageAttributes = new LinkedList<ImageAttribute>();
			for(TemplateAttribute templateAttribute : templateAttributes) {
				ImageAttribute found = null;
				for (ImageAttribute attribute : attributes) {
					if (attribute.attribute.equals(templateAttribute.name)) {
						found = attribute;
						break;
					}
				}
				if (found != null) {
					found.templated = true;
				} else {
					attributes.add(new ImageAttribute(project, image,templateAttribute.name,null,dataMode,true));
				}
			}
		}
		
		Iterator<ImageAttribute> it = attributes.iterator();
		while(it.hasNext()) {
			ImageAttribute attr = it.next();
			
			//only show data mode attributes in datamode
			if (dataMode && !attr.data) it.remove();
		}
		
	    renderJSON(attributes);  
	}
	
	public static void updateImageAttribute(ImageAttribute attribute, String value) {
		attribute.value = value;
		attribute.save();
		
		ImportExportService.tryExportData(attribute.image);
		
		renderJSON(attribute);
	}
	
	public static void deleteImageAttribute(ImageAttribute attribute) {
		attribute.delete();
		
		ImportExportService.tryExportData(attribute.image);
		
		ok();
	}
	
	public static void createAttribute(Project project, String strPath, String attribute, String value, boolean dataMode) {
		Path path = PathService.resolve(strPath);
		
		DatabaseImage image = DatabaseImage.forPath(path);
		ImageAttribute attr = image.addAttribute(project, attribute, value, dataMode);
		
		ImportExportService.tryExportData(image);
		
		renderJSON(attr);
	}
	
	public static void importFromFile(Project project, String strPath) throws IOException {
		Path path = PathService.resolve(strPath);
		
		ImportExportService.importData(project, path);
	}
	
	public static void exportToFile(String strPath) throws IOException {
		Path path = PathService.resolve(strPath);
		
		DatabaseImage image = DatabaseImage.forPath(path);
		ImportExportService.exportData(image);
	}
}