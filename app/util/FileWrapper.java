package util;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import access.AccessType;

import services.ImportExportService;
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
	public boolean isImage;
	public boolean visible;
	public long size;
	
	public FileWrapper(Project project, String display, Path path) {
		this.display = display;
		this.path = PathService.getRelativeString(path);
		this.project = project;
		DatabaseImage image = DatabaseImage.forPath(path);
		this.image = image;
		this.visible = false;
		this.size = path.toFile().length();
		
		if (this.project != null) {
			ProjectVisibleImage pvi = ProjectVisibleImage.get(project,image);
			if (pvi != null) {
				this.visible = true;
			}
		}
		
		this.isImage = PathService.isImage(path);
		this.isDir = path.toFile().isDirectory();
	}
	
	public FileWrapper(Project project, Path path) {
		this(project, path.getFileName().toString(),path);
	}
	
	public static List<FileWrapper> wrapFiles(Project project, List<Path> paths) {
		List<FileWrapper> fileWrappers = new LinkedList<FileWrapper>();
		for (Path path : paths) {
			if (path.getFileName().startsWith(".")) continue;
			fileWrappers.add(new FileWrapper(project, path));
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
