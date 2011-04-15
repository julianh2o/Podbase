package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Response;

import java.awt.image.BufferedImage;
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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.*;
import util.*;

public class ImageBrowser extends Controller {
	
	public static void index() {
		System.out.println("Here!");
		render();
	}
	
	public static void projectIndex(long projectId) {
		Project project = Project.findById(projectId);
		System.out.println("projectIndex: "+projectId);
		renderTemplate("ImageBrowser/index.html", project);
	}
	
	protected static String getRootDirectory() {
		return Play.applicationPath.getAbsolutePath() + "/data";
	}
	
	protected static boolean canAccessFile(File f) {
		String root = getRootDirectory();
		try {
			String path = f.getCanonicalPath();
			if (path.startsWith(root)) return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	public static void fetch(String path) {
		String root = getRootDirectory();
		
		File f = new File(PodbaseUtil.concatenatePaths(root,path));
		while(!f.isDirectory()) f = f.getParentFile();
		
		if (!f.exists()) error("File not Found");
		if (!canAccessFile(f)) forbidden();
	
		File[] files = f.listFiles();
		if (files == null || files.length == 0) error("No contents");
		
		Arrays.sort(files);
		
		FileWrapper[] fileWrappers = new FileWrapper[files.length];
		for (int i = 0; i<files.length; i++) {
			fileWrappers[i] = new FileWrapper(getRootDirectory(),files[i]);
		}
		
		renderJSON(fileWrappers);
	}
	
	public static void script() {
		renderTemplate("ImageBrowser/script.js");
	}
	
	public static void resolveFile(String path) {
		File imageFile = new File(PodbaseUtil.concatenatePaths(getRootDirectory(),path));

		BufferedImage image;
		try {
			image = ImageIO.read(imageFile);
			
			image = PodbaseUtil.scaleImageToFit(image,300,300);

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
	
	protected static void renderJSON(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    renderJSON(gson.toJson(o));  
	}
	
	public static void fetchInfo(String path) {
		DatabaseImage image = DatabaseImage.find("path", path).first();
		List<ImageAttribute> attributes;
		
		if (image == null)
			attributes = new LinkedList<ImageAttribute>();
		else 
			attributes = image.attributes;
		
	    renderJSON(attributes);  
	}
	
	public static void createAttribute(String path, String attribute, String value) {
		DatabaseImage image = DatabaseImage.find("path",path).first();
		ImageAttribute attr = image.addAttribute(attribute, value);
		renderJSON(attr);
	}
	
	public static void updateAttribute(long id, String value) {
		ImageAttribute attribute = ImageAttribute.findById(id);
		attribute.value = value;
		attribute.save();
		renderJSON(attribute);
	}
	
	public static void deleteAttribute(long id) {
		ImageAttribute attribute = ImageAttribute.findById(id);
		attribute.delete();
		ok();
	}
}
