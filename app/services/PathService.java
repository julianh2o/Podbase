package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	public static Path getRootImageDirectory() {
		return Paths.get(Play.applicationPath.getAbsolutePath() + "/data");
	}
	
	public static Path resolve(String rel) {
		assertPath(rel);
		return getRootImageDirectory().resolve(rel);
	}
	
	public static String getRelativeString(Path path) {
		return getRelativePath(path).toString();
	}
	
	public static Path replaceExtension(Path path, String ext) {
		String fileName = path.getFileName().toString();
		String fileNameTrunk = fileName.substring(0,fileName.lastIndexOf('.'));
		return path.getParent().resolve(fileNameTrunk+"."+ext);
	}
	
	public static Path getRelativePath(Path path) {
        Path pathAbsolute = path;
        Path pathBase = getRootImageDirectory();
        Path pathRelative = pathBase.relativize(pathAbsolute);
        return pathRelative;
	}
	
	private static boolean isValidPath(String path) {
		if (!path.startsWith("/")) return false;
		if (path.length() == 1) return true;
		if (path.endsWith("/")) return false;
		if (path.contains("..")) return false;

		return true;
	}

	public static void assertPath(String path) {
		if (!isValidPath(path)) throw new RuntimeException("Invalid path: "+path);
	}	
	
	public static List<Path> getProjectFiles(Project project) {
		if (project == null) return null;
		List<Path> files = new LinkedList<Path>();
		for (Directory dir : project.directories) {
			Path path = resolve(dir.path);
			if (path.toFile().exists()) {
				files.add(path);
			}
		}
		return files;
	}
	
	public static List<Path> filterImagesAndDirectories(List<Path> in) {
		List<Path> out = new LinkedList<Path>();
		for (Path path : in) {
			if (isImage(path) || path.toFile().isDirectory()) {
				out.add(path);
			}
		}
		return out;
	}
	
	public static boolean isImage(Path path) {
		return isImage(path.getFileName());
	}
	
	public static boolean isImage(String name) {
		String[] extensions = {"jpg","jpeg","png","tiff","tif","gif","bmp"};
		
		name = name.toLowerCase();
		for (String ext : extensions) {
			if (name.endsWith(ext)) return true;
		}
		return false;
	}
	
	public static List<Path> listPaths(Path path) {
		File parent = path.toFile();
		List<Path> paths = new LinkedList<Path>();
		for (File f : parent.listFiles()) {
			paths.add(f.toPath());
		}
		return paths;
	}
	
}
