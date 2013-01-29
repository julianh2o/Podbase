package util;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import access.AccessType;

import services.PathService;
import services.PermissionService;

import models.DatabaseImage;
import models.Project;
import models.ProjectVisibleImage;
import models.User;

public class FileWrapper {
	public String display;
	public String path;
	public DatabaseImage image;
	public Project project;
	public boolean isDir;
	public boolean visible;
	
	public FileWrapper(Project project, String display, String rootDirectory, File f) {
		this.display = display;
		this.path = f.getAbsolutePath();
		if (path.startsWith(rootDirectory)) {
			path = path.substring(rootDirectory.length());
		}
		this.project = project;
		DatabaseImage image = DatabaseImage.forPath(path);
		this.image = image;
		this.visible = false;
		
		if (this.project != null) {
			ProjectVisibleImage pvi = ProjectVisibleImage.get(project,image);
			if (pvi != null) {
				this.visible = true;
			}
		}
		this.isDir = f.isDirectory();
	}
	
	public FileWrapper(Project project, String rootDirectory, File f) {
		this(project, f.getName(),rootDirectory,f);
	}
	
	public static List<FileWrapper> wrapFiles(Project project, List<File> files) {
		List<FileWrapper> fileWrappers = new LinkedList<FileWrapper>();
		for (File f : files) {
			fileWrappers.add(new FileWrapper(project, PathService.getRootImageDirectory(),f));
		}
		
		return fileWrappers;
	}
	
	public static List<FileWrapper> visibilityFilter(Project project, User user, List<FileWrapper> files) {
		if (PermissionService.hasInheritedAccess(user,project,AccessType.EDITOR)) {
			return files;
		}
		
		List<FileWrapper> filtered = new LinkedList<FileWrapper>();
		for (FileWrapper f : files) {
			if (f.visible || f.isDir) filtered.add(f);
		}
		
		return filtered;
	}
}
