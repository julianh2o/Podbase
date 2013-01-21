package services;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import notifiers.Email;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import controllers.ImageBrowser;
import controllers.ParentController;

import access.AccessType;

import play.mvc.Util;
import util.PodbaseUtil;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.ImageAttribute;
import models.ImageSetMembership;
import models.Paper;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.User;

public class ImportExportService {
	public static File getInputFile(DatabaseImage image) {
		int dot = image.path.lastIndexOf(".");
		String trunk = image.path.substring(0,dot);
		File f = ImageBrowser.getFile(trunk + ".yml");
		
		return f;
	}
	
	public static File getOutputFile(DatabaseImage image) {
		int dot = image.path.lastIndexOf(".");
		String trunk = image.path.substring(0,dot);
		File f = ImageBrowser.getFile(trunk + ".bak.yml");
		
		return f;
	}
	
	public static Map<String, String> loadFile(File f) throws IOException {
		return loadYaml(f);
	}
	
	private static Map<String, String> loadYaml(File f) throws IOException {
	    Yaml yaml = new Yaml();
	    String contents = FileUtils.readFileToString(f);
	    return (Map<String,String>)yaml.load(contents);
	}
	
	private static void saveYaml(File f, Map<String, String> data) throws IOException {
		DumperOptions options = new DumperOptions();
	    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);		
	    
		Yaml yaml = new Yaml(options);
		String contents = yaml.dump(data);
		FileUtils.writeStringToFile(f, contents);
	}

	public static void importData(Project project, DatabaseImage dbi) throws IOException {
		File f = getInputFile(dbi);
		
		Map<String,String> data = loadFile(f);
		for (Entry<String,String> entry : data.entrySet()) {
			dbi.addAttribute(project, entry.getKey(), entry.getValue(), true);
		}
	}
		
	public static void importData(Project project, String path) throws IOException {
		DatabaseImage dbi = DatabaseImage.forPath(path);
		importData(project,dbi);
	}
	
	public static void tryExportData(DatabaseImage dbi) {
		try {
			exportData(dbi);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exportData(DatabaseImage dbi) throws IOException {
		File f = getOutputFile(dbi);
		
		Map<String,String> data = new HashMap<String,String>();
		for (ImageAttribute attr : dbi.attributes) {
			data.put(attr.attribute, attr.value);
		}
		
		saveYaml(f,data);
	}
	
	public static void importDirectoryRecursive(final Project project, File f) throws IOException {
		Files.walkFileTree(f.toPath(),new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (PodbaseUtil.isImage(file.toFile())) {
					importData(project,PodbaseUtil.imageForFile(file.toFile()));
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
