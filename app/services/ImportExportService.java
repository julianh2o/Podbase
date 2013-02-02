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
	public static Path getInputFile(DatabaseImage image) {
		return PathService.replaceExtension(image.getPath(),"yml");
	}
	
	public static Path getOutputFile(DatabaseImage image) {
		return PathService.replaceExtension(image.getPath(),"bak.yml");
	}
	
	public static Path getImportedFile(DatabaseImage image) {
		return PathService.replaceExtension(image.getPath(),"imported.yml");
	}
	
	public static Map<String, String> loadFile(Path path) throws IOException {
		return loadYaml(path);
	}
	
	private static Map<String, String> loadYaml(Path path) throws IOException {
	    Yaml yaml = new Yaml();
	    String contents = FileUtils.readFileToString(path.toFile());
	    return (Map<String,String>)yaml.load(contents);
	}
	
	private static void saveYaml(Path path, Map<String, String> data) throws IOException {
		DumperOptions options = new DumperOptions();
	    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);		
	    
		Yaml yaml = new Yaml(options);
		String contents = yaml.dump(data);
		FileUtils.writeStringToFile(path.toFile(), contents);
	}

	public static void importData(Project project, DatabaseImage dbi) throws IOException {
		Path path = getInputFile(dbi);
		
		Map<String,String> data = loadFile(path);
		for (Entry<String,String> entry : data.entrySet()) {
			dbi.addAttribute(project, entry.getKey(), entry.getValue(), true);
		}
	}
	
	public static boolean hasFileToImport(DatabaseImage image) {
		return getInputFile(image).toFile().exists();
	}
	
	public static List<DatabaseImage> findImportables(Path path) {
		final List<DatabaseImage> importable = new LinkedList<DatabaseImage>();
		try {
			Files.walkFileTree(path,new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					if (PathService.isImage(path)) {
						DatabaseImage image = DatabaseImage.forPath(path);
						if (hasFileToImport(image)) {
							importable.add(image);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException io) {
			io.printStackTrace();
			return importable;
		}
		return importable;
	}
		
	public static void importData(Project project, Path path) throws IOException {
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
		Path path = getOutputFile(dbi);
		
		Map<String,String> data = new HashMap<String,String>();
		for (ImageAttribute attr : dbi.attributes) {
			data.put(attr.attribute, attr.value);
		}
		
		saveYaml(path,data);
	}
	
	public static void importDirectoryRecursive(final Project project, Path root) throws IOException {
		Files.walkFileTree(root,new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				if (PathService.isImage(path)) {
					importData(project,DatabaseImage.forPath(path));
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
