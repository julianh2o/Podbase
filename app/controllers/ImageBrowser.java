package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.Response;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.*;
import util.*;

public class ImageBrowser extends Controller {
	
	public static void index() {
		render();
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
		if (!canAccessFile(f)) forbidden();
		
		File[] files = f.listFiles();
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

	
	public static void fetchInfo(String path) {
		DatabaseImage image = DatabaseImage.find("byPath", path).first();
		
	    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	    renderJSON(gson.toJson(image.attributes));  
	}
}
