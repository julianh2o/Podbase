package controllers;

import play.*;
import play.mvc.*;

import java.io.File;
import java.util.*;

import models.*;
import util.*;

public class ImageBrowser extends Controller {
	
	public static void index() {
		render();
	}
	
	protected static String getRootDirectory() {
		return Play.applicationPath.getAbsolutePath() + "/public";
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

}
