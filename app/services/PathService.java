package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;

import play.Play;
import play.mvc.Util;
import util.PodbaseUtil;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.ImageSetMembership;
import models.Paper;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.User;

//TODO unify path representation
public class PathService {
	public static String getRootImageDirectory() {
		return Play.applicationPath.getAbsolutePath() + "/data";
	}
	
	public static File getRootImageDirectoryFile() {
		return new File(getRootImageDirectory());
	}
	
	public static String relativeToRoot(String rel) {
		return concatenatePaths(getRootImageDirectory(), rel);
	}
	
	public static File getFile(String path) {
		assertPath(path);
		File imageFile = new File(PathService.concatenatePaths(PathService.getRootImageDirectory(),path));
		return imageFile;
	}
	
	public static boolean isValidPath(String path) {
		if (!path.startsWith("/")) return false;
		if (path.length() == 1) return true;
		if (path.endsWith("/")) return false;
		if (path.contains("..")) return false;

		return true;
	}

	public static void assertPath(String path) {
		if (!isValidPath(path)) throw new RuntimeException("Invalid path: "+path);
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
	
	public static List<File> filterImages(List<File> in) {
		List<File> out = new LinkedList<File>();
		for (File f : in) {
			if (isImage(f)) {
				out.add(f);
			}
		}
		return out;
	}
	
	public static boolean isImage(File file) {
		return isImage(file.getName());
	}
	
	public static boolean isImage(String name) {
		String[] extensions = {"jpg","jpeg","png","tiff","tif","gif","bmp"};
		
		name = name.toLowerCase();
		for (String ext : extensions) {
			if (name.endsWith(ext)) return true;
		}
		return false;
	}
	
	public static String concatenatePaths(String path, String rel) {
		String sep = "/";
		if (path.endsWith("/") || rel.startsWith("/")) sep = "";
		return path + sep + rel;
	}
	
	public static DatabaseImage imageForFile(File f) {
        Path pathAbsolute = f.toPath();
        Path pathBase = getRootImageDirectoryFile().toPath();
        Path pathRelative = pathBase.relativize(pathAbsolute);
        String path = "/"+pathRelative.toString();
        return DatabaseImage.forPath(path);
	}
	
	//TODO toss me?
	public static File getDirectory(String path) throws FileNotFoundException {
		File f = new File(concatenatePaths(getRootImageDirectory(),path));
		while(!f.isDirectory()) f = f.getParentFile();
		
		if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());
	
		return f;
	}
}
