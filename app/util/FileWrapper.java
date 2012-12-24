package util;

import java.io.File;

import models.DatabaseImage;
import models.Project;
import models.ProjectVisibleImage;

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
}
