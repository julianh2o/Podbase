package services;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import notifiers.Email;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.yaml.snakeyaml.Yaml;

import controllers.ImageBrowser;
import controllers.ParentController;

import access.AccessType;

import play.mvc.Util;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
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
	
	public static Map<String, String> loadFile(File f) throws IOException {
		return loadYaml(f);
	}
	
	private static Map<String, String> loadYaml(File f) throws IOException {
	    Yaml yaml = new Yaml();
	    String contents = FileUtils.readFileToString(f);
	    return (Map<String,String>)yaml.load(contents);
	}

	public static void importData(Project project, String path) throws IOException {
		DatabaseImage dbi = DatabaseImage.forPath(path);
		File f = getInputFile(dbi);
		
		Map<String,String> data = loadFile(f);
		for (Entry<String,String> entry : data.entrySet()) {
			dbi.addAttribute(project, entry.getKey(), entry.getValue(), true);
		}
	}
}
