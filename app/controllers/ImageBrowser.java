package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Response;

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
import java.io.IOException;
import java.lang.annotation.Annotation;
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
import services.PermissionService;
import util.*;

public class ImageBrowser extends ParentController {
	
	public static void index() {
		render();
	}
	
	public static String getRootImageDirectory() {
		return Play.applicationPath.getAbsolutePath() + "/data";
	}
	
	public static File getRootImageDirectoryFile() {
		return new File(getRootImageDirectory());
	}
	
	public static boolean canAccessFile(Project project, File f) {
		String root = getRootImageDirectory();
		try {
			String path = f.getCanonicalPath();
			if (path.startsWith(root)) return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	// If this is passed a path like.. "/foo/bar/baz.png"
	// It'll return the file for "/foo/bar"
	public static File getDirectory(Project project, String path) {
		PodbaseUtil.assertPath(path);
		
		File f = new File(PodbaseUtil.concatenatePaths(getRootImageDirectory(),path));
		while(!f.isDirectory()) f = f.getParentFile();
		
		if (!f.exists()) error("File not Found");
		if (!canAccessFile(project, f)) forbidden();
	
		return f;
	}
	
	public static String relativeToRoot(String rel) {
		return PodbaseUtil.concatenatePaths(getRootImageDirectory(), rel);
	}
	
	public static List<File> getProjectFiles(Project project) {
		if (project == null) return null;
		List<File> files = new LinkedList<File>();
		for (Directory dir : project.directories) {
			File f = new File(relativeToRoot(dir.path));
			if (f.exists()) files.add(f);
		}
		return files;
	}
	
	@ProjectAccess(AccessType.VISIBLE)
	public static void fetchProjectPath(Project project, String path) {
		PodbaseUtil.assertPath(path);
		
		List<File> projectFiles = getProjectFiles(project);
		File directory = getDirectory(project, path);
		final boolean isRoot = directory.equals(getRootImageDirectoryFile());
		if (isRoot) {
			renderJSON(wrapFiles(projectFiles));
		} else {
			renderJSON(wrapFiles(Arrays.asList(directory.listFiles())));
		}
	}
	
	@Access(AccessType.ROOT)
	public static void fetchPath(String path) {
		PodbaseUtil.assertPath(path);
		
		File directory = getDirectory(null, path);
		renderJSON(wrapFiles(Arrays.asList(directory.listFiles())));
	}
	
	@Util
	public static List<FileWrapper> wrapFiles(List<File> files) {
		Collections.sort(files);
		
		List<FileWrapper> fileWrappers = new LinkedList<FileWrapper>();
		for (File f : files) {
			fileWrappers.add(new FileWrapper(getRootImageDirectory(),f));
		}
		
		return fileWrappers;
	}
	
	static BufferedImage getImage(String path) {
		PodbaseUtil.assertPath(path);
		
		User user = Security.getUser();
		File imageFile = new File(PodbaseUtil.concatenatePaths(getRootImageDirectory(),path));
		
		boolean hasAccess = PermissionService.userCanAccessImage(user,path);
		System.out.println("Access: "+hasAccess);
		if (!hasAccess) forbidden();

		try {
			return ImageIO.read(imageFile);
		} catch (IOException e) {
			return null;
		}
			
	}
	
	public static void resolveFile(String path, String mode, Project project, Float scale, Integer width, Integer height, Float brightness, Float contrast, Boolean histogram) {
		PodbaseUtil.assertPath(path);
		
		BufferedImage image = getImage(path);
		if (image == null) error("Image not found");
		
		try {
			if ("thumb".equals(mode)) {
				image = PodbaseUtil.scaleImageToFit(image,200,200);
			} else if ("fit".equals(mode) && width != null && height != null) {
				image = PodbaseUtil.scaleImageToFit(image,width,height);
			} else if (width != null && height != null) {
				image = PodbaseUtil.scaleImage(image,width,height);
			} else if (width != null || height != null) {
				image = PodbaseUtil.scaleImageToFit(image,width,height);
			} else if (scale != null) {
				image = PodbaseUtil.scaleImage(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
			}
			
			if (histogram == null) histogram = false;
			if (brightness == null) brightness = (float)0;
			if (contrast == null) contrast = (float)1;
			
			ImagePlus img = new ImagePlus("image",image);
			double range = 255;
			double center = 128;
			range = range / contrast;
			center = center - brightness;
			double min = center-range/2.0;
			double max = center+range/2.0;
			img.setDisplayRange(min,max);
			image = img.getBufferedImage();
			
			if (histogram) {
				int textHeight = 14;
				BufferedImage hist = PodbaseUtil.makeHistogram(image, image.getWidth(), image.getWidth()/2);
				
				BufferedImage imageWithHist = new BufferedImage(image.getWidth(), image.getHeight()+hist.getHeight()+textHeight,image.getType());
				Graphics g = imageWithHist.getGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0,0,imageWithHist.getWidth(),imageWithHist.getHeight());
				g.drawImage(image, 0, 0, null);
				g.drawImage(hist, 0, image.getHeight()+textHeight,null);
				g.setColor(Color.BLACK);
				g.drawString("Histogram", 5, image.getHeight()+textHeight-2);
				image = imageWithHist;
			}
			
			//ImageInputStream is = ImageIO.createImageInputStream(image);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			Response.current().contentType = "image/png";

			//TODO cache this
			renderBinary(bais);
		} catch (IOException e) {
			error(e);
		}
	}
	
	public static void fetchInfo(Project project, String path, boolean dataMode) {
		PodbaseUtil.assertPath(path);
		
		//TODO fix this by using inherited template assignments
		String usePath =  path.substring(0,path.lastIndexOf("/")+1);
		TemplateAssignment assignment = TemplateAssignment.forPath(project,usePath);
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
	
	public static void createAttribute(Project project, String path, String attribute, String value, boolean dataMode) {
		PodbaseUtil.assertPath(path);
		
		DatabaseImage image = DatabaseImage.forPath(path);
		ImageAttribute attr = image.addAttribute(project, attribute, value, dataMode);
		renderJSON(attr);
	}
}
