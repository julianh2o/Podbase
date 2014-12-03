// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

public class PathService {
	public static Path getApplicationPath() {
		File applicationPath = Play.applicationPath;
		if (applicationPath == null) applicationPath = new File(".");
		return applicationPath.toPath();
	}
	
	public static Path getOutputDirectory() {
		return getApplicationPath().resolve("out");
	}
	
	public static File prepareOutputFile(String name) {
		getOutputDirectory().toFile().mkdirs();
		return getOutputDirectory().resolve(name).toFile();
	}
	
	public static Path getRootImageDirectory() {
		return Paths.get(getApplicationPath() + "/data");
	}
	
	public static Path resolve(String rel) {
		assertPath(rel);
		return getRootImageDirectory().resolve("."+rel).normalize();
	}
	
	public static Path fixCaseResolve(String rel) throws FileNotFoundException {
		return fixCaseResolve(getRootImageDirectory(),rel);
	}
	
	public static Path fixCaseResolve(Path base, String rel) throws FileNotFoundException {
		if (rel.startsWith("/")) rel = rel.substring(1);
		
		String[] split = rel.split("/",2);
		String folderName = split[0];
		Path found = null;
		
		for (Path path : listPaths(base)) {
			if (path.getFileName().toString().equalsIgnoreCase(folderName)) {
				found = path;
			}
		}
		
		if (found == null) throw new FileNotFoundException("File not found: "+rel);
		
		if (split.length == 1) return found;
		
		return fixCaseResolve(found,split[1]);
	}
	
	public static Path getRelativePath(Path path) {
        Path pathAbsolute = path;
        Path pathBase = getRootImageDirectory();
        Path pathRelative = pathBase.relativize(pathAbsolute);
        return pathRelative;
	}
	
	public static File relativePathToFile(Path path) {
		return getRootImageDirectory().resolve(path).toFile();
	}
	
	public static String getRelativeString(Path path) {
		return "/"+getRelativePath(path).toString();
	}
	
	public static Path replaceExtension(Path path, String ext) {
		String fileName = path.getFileName().toString();
		int index = fileName.lastIndexOf('.');
		
		if (index == -1) return path.getParent().resolve(fileName+"."+ext);
		
		String fileNameTrunk = fileName.substring(0,index);
		return path.getParent().resolve(fileNameTrunk+"."+ext);
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
	
	public static boolean isPathInProject(Path path, Project project) {
		for (Directory dir : project.directories) {
			if (path.startsWith(dir.getPath())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static List<Path> getParentDirectories(Path path) {
		path = getRelativePath(path);
		List<Path> directories = new LinkedList<Path>();
		while (!getRootImageDirectory().equals(path) && path != null) {
			if (relativePathToFile(path).isDirectory()) directories.add(path);
			path = path.getParent();
		}
		return directories;
	}
	
	//TODO make this more efficient
	public static Project projectForPath(Path path) {
		List<Project> projects = Project.all().fetch();
		for(Project project : projects) {
			if (isPathInProject(path,project)) return project;
		}
		
		return null;
	}
	
	public static List<Path> filterImagesAndDirectories(List<Path> in) {
		List<Path> out = new LinkedList<Path>();
		for (Path path : in) {
			if (!isYaml(path) || path.toFile().isDirectory()) {
				out.add(path);
			}
		}
		return out;
	}
	
	public static boolean isImage(Path path) {
		return isImage(path.getFileName().toString());
	}
	
	public static boolean isYaml(Path path) {
		return path.getFileName().toString().toLowerCase().endsWith("yml");
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
		if (!parent.exists()) return null;
		for (File f : parent.listFiles()) {
			paths.add(f.toPath());
		}
		return paths;
	}
	
	public static String calculateImageHash(Path path) throws IOException {
		FileInputStream fis = new FileInputStream(path.toFile());
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
		fis.close();
		return md5;	
	}
	
	public static Path calculateHashFolderPath(Path baseDir, String requestHash) {
		int levels = 2;
		int size = 2;
		
		int index = 0;
		Path currentDir = baseDir;
		for (int i=0; i<levels; i++) {
			String folderName = requestHash.substring(index,index+size);
			index += size;
			currentDir = currentDir.resolve(folderName);
		}
		return currentDir.resolve(requestHash.substring(index));
	}
}
