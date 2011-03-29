package util;

import java.io.File;

public class FileWrapper {
	public String path;
	public boolean isDir;
	public FileWrapper(String rootDirectory, File f) {
		this.path = f.getAbsolutePath();
		if (path.startsWith(rootDirectory)) {
			path = path.substring(rootDirectory.length());
		}
		this.isDir = f.isDirectory();
	}
}
