package util;

import java.io.File;

public class FileWrapper {
	public String display;
	public String path;
	public boolean isDir;
	
	public FileWrapper(String display, String rootDirectory, File f) {
		this.display = display;
		this.path = f.getAbsolutePath();
		if (path.startsWith(rootDirectory)) {
			path = path.substring(rootDirectory.length());
		}
		this.isDir = f.isDirectory();
	}
	
	public FileWrapper(String rootDirectory, File f) {
		this(f.getName(),rootDirectory,f);
	}
}
