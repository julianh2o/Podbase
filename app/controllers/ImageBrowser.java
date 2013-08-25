// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import play.*;
import play.modules.search.Search;
import play.mvc.*;
import play.mvc.Http.Response;

import groovy.lang.DeprecationException;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;

import access.Access;
import access.AccessType;
import access.ModelAccess;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import models.*;
import services.DatabaseImageService;
import services.ImageService;
import services.ImportExportService;
import services.PathService;
import services.PermissionService;
import util.*;

@With(Security.class)
public class ImageBrowser extends ParentController {
	public static void index() {
		render();
	}
	
	@ModelAccess(AccessType.LISTED)
	public static void fetchProjectPath(Project project, Path path) throws FileNotFoundException {
		List<Path> paths = PathService.listPaths(path);
		
		paths = PathService.filterImagesAndDirectories(paths);
		
		List<FileWrapper> wrappedFiles = FileWrapper.wrapFiles(project, paths);
		wrappedFiles = FileWrapper.visibilityFilter(project, Security.getUser(), wrappedFiles);
		Collections.sort(wrappedFiles,FileWrapper.getComparator());
		renderJSON(wrappedFiles);
	}
	
	@ModelAccess(AccessType.SET_VISIBLE)
	public static void setVisible(Project project, DatabaseImage image, boolean visible) {
		ProjectVisibleImage.setVisible(project,image,visible);
	}
	
	@ModelAccess(AccessType.SET_VISIBLE)
	public static void setMultipleVisible(Project project, String ids, boolean visible) {
		for (String id : ids.split(",")) {
			long longid = Long.parseLong(id);
			DatabaseImage image = DatabaseImage.findById(longid);
			ProjectVisibleImage.setVisible(project,image,visible);
		}
	}
	
	@Util
	public static ImagePlus getImage(Path path) {
		User user = Security.getUser();
		
		boolean hasAccess = PermissionService.userCanAccessPath(user,path);
		if (!hasAccess) forbidden();

		Opener opener = new Opener();
		ImagePlus ip = opener.openImage(path.toAbsolutePath().toString());
		return ip;
	}
	
	@ModelAccess(AccessType.LISTED)
	public static void imageMetadata(Path path) throws IOException {
		ImagePlus image = getImage(path);
		
		HashMap<String,Object> info = new HashMap<String,Object>();
		info.put("slices", image.getStackSize());
	
		renderJSON(info);
	}
	
	@ModelAccess(AccessType.LISTED)
	public static void resolveFile(Path path, String mode, Project project, Float scale, Integer width, Integer height, Float brightness, Float contrast, Boolean histogram, Integer slice) throws IOException {
		if (!PermissionService.userCanAccessPath(Security.getUser(),path)) forbidden();
		
		if (params._contains("download")) response.setHeader("Content-Disposition", "attachment; filename="+path.getFileName());
		
		if (!PathService.isImage(path)) {
			renderBinary(path.toFile());
			return;
		}
		
		ImagePlus image = getImage(path);
		if (slice != null) {
			if (slice < 1 || slice > image.getStackSize()) throw new RuntimeException("Invalid slice! Found: "+slice+" max is "+image.getStackSize());
			image.setSlice(slice);
		}
		
		if ("thumb".equals(mode)) {
			image = ImageService.scaleImageToFit(image,200,200);
		} else if ("fit".equals(mode) && width != null && height != null) {
			image = ImageService.scaleImageToFit(image,width,height);
		} else if (width != null && height != null) {
			image = ImageService.scaleImage(image,width,height);
		} else if (width != null || height != null) {
			image = ImageService.scaleImageToFit(image,width,height);
		} else if (scale != null) {
			image = ImageService.scaleImage(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		}
		
		if (histogram == null) histogram = false;
		if (brightness == null) brightness = (float)0;
		if (contrast == null) contrast = (float)0;
		
		image = ImageService.adjustImage(image, brightness, contrast);
		
		if (histogram) {
			System.out.println("REIMPLEMENT ME!");
			//BufferedImage hist = ImageService.makeHistogram(image, image.getWidth(), image.getWidth()/2);
			//image = ImageService.appendImages(image,hist);
		}
		
		//TODO cache this
		BufferedImage imageOut = image.getBufferedImage();
		boolean noWatermark = PermissionService.hasInheritedAccess(Security.getUser(), PathService.projectForPath(path), AccessType.NO_WATERMARK);
		if (!noWatermark) imageOut = ImageService.addWatermark(imageOut);
		renderImage(imageOut);
	}
	
	//TODO remove me
	public static void checkHash(Path path) throws IOException {
		DatabaseImage image = DatabaseImage.forPath(path);
		StringBuffer sb = new StringBuffer();
		sb.append("Stored hash: "+image.hash+"\n");
		String imageHash = PathService.calculateImageHash(path);
		sb.append("File hash:   "+imageHash);
		renderText(sb.toString());
	}
	
	@ModelAccess(AccessType.LISTED)
	public static void downloadAttributes(Project project, Path path, boolean dataMode) {
		if (!PermissionService.userCanAccessPath(Security.getUser(),path)) forbidden();
		
		DatabaseImage image = DatabaseImage.forPath(path);
		TemplateAssignment assignment = TemplateController.templateForPath(project, path);
		Template template = assignment==null?null:assignment.template;
		
	    List<ImageAttribute> attributes = DatabaseImageService.compileAttributesForImage(project, image, template, dataMode, false);
		String data = ImportExportService.serializeAttributes(attributes);
		
		String name = image.getPath().getFileName().toString();
		
		response.setHeader("Content-Disposition", "attachment; filename="+name+".yml");
		renderText(data);
	}
	
	public static void importAttributes(Project project, Path path, File file, boolean dataMode) throws IOException {
		if (!PermissionService.userCanAccessPath(Security.getUser(),path)) forbidden();
		
		DatabaseImage dbi = DatabaseImage.forPath(path);
		String data = FileUtils.readFileToString(file);
		
		Map<String,String> importedAttributes = ImportExportService.deserialzeAttributes(data);
		
		//remove all existing attributes
		List<ImageAttribute> existingAttributes = DatabaseImageService.attributesForImageAndMode(project,dbi,dataMode);
		for (ImageAttribute attr : existingAttributes) {
			if (attr.linkedAttribute != null) {
				attr.linkedAttribute.linkedAttribute = null;
				attr.linkedAttribute.save();
				attr.linkedAttribute = null;
			}
			attr.delete();
		}
		
		//if we're in analysis mode, we need to consider inherited attributes
		Map<String,List<ImageAttribute>> dataModeAttributes = DatabaseImageService.attributeMapForImageAndMode(project, dbi, !dataMode);
		HashSet<ImageAttribute> usedImageAttributes = new HashSet<ImageAttribute>();
		
		
		//get the imported attributes grouped by attributeName
		Map<String,List<String>> groupedAttributes = new HashMap<String,List<String>>();
		for (Entry<String,String> entry : importedAttributes.entrySet()) {
			if (!groupedAttributes.containsKey(entry.getKey())) groupedAttributes.put(entry.getKey(), new LinkedList<String>());
			groupedAttributes.get(entry.getKey()).add(entry.getValue());
		}
		for (Entry<String, List<String>> entry : groupedAttributes.entrySet()) {
			String attributeName = entry.getKey();
			for (String value : entry.getValue()) {
				List<ImageAttribute> existingAttributesForKey = dataModeAttributes.get(attributeName);
				ImageAttribute linkMe = null;
				if (dataModeAttributes.containsKey(attributeName)) {
					for (ImageAttribute existingAttr : existingAttributesForKey) {
						if (!usedImageAttributes.contains(existingAttr)) {
							linkMe = existingAttr;
						}
					}
				}
				
				ImageAttribute newImageAttribute = dbi.addAttribute(project, attributeName, value, dataMode);
				if (linkMe != null) {
					usedImageAttributes.add(linkMe);
					newImageAttribute.linkedAttribute = linkMe;
					newImageAttribute.save();
					
					linkMe.linkedAttribute = newImageAttribute;
					linkMe.save();
				}
			}
		}
		
		jsonOk();
	}
	
	@ModelAccess(AccessType.LISTED)
	public static void fetchInfo(Project project, Path path, boolean dataMode) {
		TemplateAssignment assignment = TemplateController.templateForPath(project, path);
		Template template = assignment==null?null:assignment.template;
		DatabaseImage image = DatabaseImage.forPath(path);
		
	    renderJSON( DatabaseImageService.compileAttributesForImage(project, image, template, dataMode, true) );
	}
	
	//TODO Access control is done in the method.. maybe this could be enhanced?
	public static void updateImageAttribute(ImageAttribute attribute, String value, boolean dataMode) {
		if (!permitMetadataEdit(attribute.project, dataMode)) forbidden();
		
		if (attribute.value == value) return;
		
		if (dataMode != attribute.data) {
			ImageAttribute newAttribute = attribute.image.addAttribute(attribute.project,attribute.attribute,value,dataMode);
			
			newAttribute.linkedAttribute = attribute;
			newAttribute.save();
			
			attribute.linkedAttribute = newAttribute;
			attribute.save();
			
			attribute = newAttribute;
		}
		attribute.value = value;
		attribute.save();
		
		ImportExportService.tryExportData(attribute.image);
		
		renderJSON(attribute);
	}
	
	@Util
	public static boolean permitMetadataEdit(Project project, boolean dataMode) {
		boolean permitted;
		if (dataMode) {
			permitted = PermissionService.hasInheritedAccess(Security.getUser(),project, AccessType.EDIT_DATA_METADATA);
		} else {
			permitted = PermissionService.hasInheritedAccess(Security.getUser(),project, AccessType.EDIT_ANALYSIS_METADATA);
		}
		return permitted;
	}
	
	public static void deleteImageAttribute(ImageAttribute attribute) {
		if (!permitMetadataEdit(attribute.project, attribute.data)) forbidden();
		
		attribute.delete();
		
		ImportExportService.tryExportData(attribute.image);
		
		ok();
	}
	
	public static void createAttribute(Project project, Path path, String attribute, String value, boolean dataMode) {
		if (!permitMetadataEdit(project, dataMode)) forbidden();
		
		DatabaseImage image = DatabaseImage.forPath(path);
		ImageAttribute attr = image.addAttribute(project, attribute, value, dataMode);
		
		ImportExportService.tryExportData(image);
		
		renderJSON(attr);
	}
	
	private class KeyValueStore {
		String attribute;
		String value;
		
		public String toString() {
			return attribute + ": "+value;
		}
	}
	
	public static void pasteAttributes(Project project, Path path, String jsonAttributes, boolean overwrite, boolean dataMode) {
		if (!permitMetadataEdit(project, dataMode)) forbidden();
		
		Stopwatch sw = new Stopwatch();
		sw.start("loading image");
		DatabaseImage image = DatabaseImage.forPath(path);
		sw.stop("loading image");
		
		sw.start("parsing json");
		Type listType = new TypeToken<List<KeyValueStore>>() {}.getType();
		List<KeyValueStore> attributes = new GsonBuilder().create().fromJson(jsonAttributes, listType);
		sw.stop("parsing json");
		
		sw.start("get map");
		Map<String,List<ImageAttribute>> existingMap = DatabaseImageService.attributeMapForImageAndMode(project, image, dataMode);
		sw.start("get map");
		
		sw.start("loop");
		for (KeyValueStore attr : attributes) {
			if (attr.value ==  null) continue;
			sw.start("loopinner");
			if (existingMap.containsKey(attr.attribute)) {
				if (!overwrite) continue;
				
				sw.start("delete all");
				for (ImageAttribute iattr : existingMap.get(attr.attribute)) {
					iattr.delete();
				}
				sw.stop("delete all");
			}
			
			if (attr.value.trim().isEmpty()) continue;
			sw.start("add attr");
			image.addAttribute(project, attr.attribute, attr.value, dataMode);
			sw.stop("add attr");
			sw.stop("loopinner");
		}
		sw.stop("loop");
		
		sw.start("export data");
		ImportExportService.tryExportData(image);
		sw.stop("export data");
		
		System.out.println(sw.toString());
		jsonOk();
	}
	
	@ModelAccess(AccessType.OWNER)
	public static void importFromFile(Project project, Path path) throws IOException {
		ImportExportService.importData(project, path);
	}
	
	@ModelAccess(AccessType.LISTED)
	public static void findImportables(Path path) {
		renderJSON(ImportExportService.findImportables(path));
	}
	
	@ModelAccess(AccessType.OWNER)
	public static void importDirectory(Project project, Path path) throws IOException {
		ImportExportService.importDirectoryRecursive(project, path);
	}
	
	@ModelAccess(AccessType.FILE_UPLOAD)
	public static void upload(File file, Path path) {
		File destination = path.resolve(file.getName()).toFile();
		try {
			FileUtils.copyFile(file, destination);
		} catch (IOException e) {
			error(e);
		}
		ok();
	}
	
	@ModelAccess(AccessType.FILE_UPLOAD)
	public static void createDirectory(Path path) {
		path.toFile().mkdir();
		ok();
	}
	
	@ModelAccess(AccessType.FILE_DELETE)
	public static void deleteFile(Path path) {
		path.toFile().delete();
		ok();
	}
}