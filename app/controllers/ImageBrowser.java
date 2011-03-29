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
		String path = f.getAbsolutePath();
		if (path.startsWith(root)) return true;
		return false;
	}
	
	public static void fetch(String path) {
		String root = getRootDirectory();
		
		File f = new File(PodbaseUtil.concatenatePaths(root,path));
		if (!canAccessFile(f)) error("Invalid path");
		
		File[] files = f.listFiles();
		renderJSON(files);
	}
	
	public static void script() {
		renderTemplate("ImageBrowser/script.js");
	}

}
