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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
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
		if (paths == null) {
			renderJSON(new LinkedList<FileWrapper>());
			return;
		}
		
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
	public static void setMultipleVisible(Project project, String rootPath, String names, boolean visible) {
		for (String name : names.split(",")) {
			Path path = PathService.resolve(rootPath+"/"+name);
			if (path.toFile().isDirectory()) {
				recursiveSetVisibility(project, path, visible);
			} else {
				DatabaseImage image = DatabaseImage.forPath(path);
				ProjectVisibleImage.setVisible(project,image,visible);
			}
		}
	}
	
	@Util
	public static void recursiveSetVisibility(Project project, Path path, boolean visible) {
		for (Path p : PathService.listPaths(path)) {
			if (p.toFile().isDirectory()) {
				recursiveSetVisibility(project,p,visible);
			} else {
				DatabaseImage image = DatabaseImage.forPath(p);
				ProjectVisibleImage.setVisible(project,image,visible);
			}
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
		Boolean noWatermark = PermissionService.hasInheritedAccess(Security.getUser(), PathService.projectForPath(path), AccessType.NO_WATERMARK);
		
		if (params._contains("download")) response.setHeader("Content-Disposition", "attachment; filename="+path.getFileName());
		
		if (!PathService.isImage(path)) {
			renderBinary(path.toFile());
			return;
		}
		
		String argumentString = PodbaseUtil.argumentString(path,mode,scale,width,height,brightness,contrast,histogram,slice,noWatermark);
		String argumentHash = PodbaseUtil.argumentHash(path,mode,scale,width,height,brightness,contrast,histogram,slice,noWatermark);
		Path cacheFolder = PathService.getApplicationPath().resolve("./tmp/cache");
		Path cachedImagePath = PathService.calculateHashFolderPath(cacheFolder,argumentHash+".png");
		Path cacheMetadataPath = PathService.calculateHashFolderPath(cacheFolder,argumentHash+".txt");
		File cachedImageFile = cachedImagePath.toFile();
		if (cacheMetadataPath.toFile().exists()) {
			String cachedArguments = FileUtils.readFileToString(cacheMetadataPath.toFile());
			if (cachedArguments.equals(argumentString)) {
				//cache hit
				//System.out.println("Cache HIT");
				BufferedImage bi = ImageIO.read(cachedImageFile);
				renderImage(bi);
			}
		}
		//System.out.println("cache miss");
		//cache miss
		
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
		
		BufferedImage imageOut = image.getBufferedImage();
		if (!noWatermark) imageOut = ImageService.addWatermark(imageOut);
		try {
			if (!cachedImageFile.exists()) {
				cachedImageFile.getParentFile().mkdirs();
				cachedImageFile.createNewFile();
				cacheMetadataPath.toFile().createNewFile();
			}
			ImageIO.write(imageOut, "png", cachedImageFile);
			FileUtils.write(cacheMetadataPath.toFile(), argumentString);
		} catch (IOException e) {
			System.err.println("Failed to write cached image");
			e.printStackTrace();
		}
		renderImage(imageOut);
	}
	
	//TODO remove me
//	public static void checkHash(Path path) throws IOException {
//		DatabaseImage image = DatabaseImage.forPath(path);
//		StringBuffer sb = new StringBuffer();
//		sb.append("Stored hash: "+image.hash+"\n");
//		String imageHash = PathService.calculateImageHash(path);
//		sb.append("File hash:   "+imageHash);
//		renderText(sb.toString());
//	}
	
	@ModelAccess(AccessType.LISTED)
	public static void downloadAttributes(Project project, Path path, boolean dataMode) {
		if (dataMode && !PermissionService.hasInheritedAccess(Security.getUser(), project,AccessType.SET_DATA_MODE)) dataMode = false; //TODO change this
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
		if (!PermissionService.hasInheritedAccess(Security.getUser(), project,dataMode ? AccessType.EDIT_DATA_METADATA : AccessType.EDIT_ANALYSIS_METADATA)) forbidden();
		if (!PermissionService.userCanAccessPath(Security.getUser(),path)) forbidden();
		
		DatabaseImage dbi = DatabaseImage.forPath(path);
		String data = FileUtils.readFileToString(file);
		
		Map<String,String> importedAttributes = ImportExportService.deserialzeAttributes(data);
		
		//remove all existing attributes
		//TODO rewrite this so that history is persisted
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
		if (image == null) return;
		
	    renderJSON( DatabaseImageService.compileAttributesForImage(project, image, template, dataMode, true) );
	}
	
	public static void updateImageAttribute(ImageAttribute attribute, String value, String comment, boolean dataMode) {
		if (!permitMetadataEdit(attribute.project, dataMode)) forbidden();
		
		if (attribute.value == value) return;
		
		if (dataMode != attribute.data) {
			ImageAttribute newAttribute = attribute.image.addAttribute(attribute.project,attribute.attribute,value,dataMode);
			
			newAttribute.linkedAttribute = attribute;
			newAttribute.value = value;
			newAttribute.save();
			
			attribute.linkedAttribute = newAttribute;
			attribute.save();
			
			attribute = newAttribute;
		} else {
			attribute.updateAttribute(Security.getUser(), value, comment);
		}
		
		ImportExportService.tryExportData(attribute.image);
		
		renderJSON(attribute);
	}
	
	public static void fetchAttributeHistory(ImageAttribute attribute) {
		User currentUser = Security.getUser();
		if (!PermissionService.hasInheritedAccess(currentUser, attribute.project, attribute.data ? AccessType.VIEW_DATA_HISTORY : AccessType.VIEW_ANALYSIS_HISTORY)) forbidden();
		renderJSON(attribute.history);
	}
	
	public static void attributeSearchReplace(Path path, String search, String replace, boolean recursive, boolean confirmReplace) {
		Project project = PathService.projectForPath(path);
		if (!permitMetadataEdit(project, false) || !permitMetadataEdit(project,true)) forbidden();
		
		String rel = PathService.getRelativeString(path);
		String imageQuery = String.format("SELECT i FROM DatabaseImage i WHERE path LIKE '%s' AND path NOT LIKE '%s' AND hash IS NOT NULL",rel+"/%",rel+"/%/%");
		if (recursive) imageQuery = String.format("SELECT i FROM DatabaseImage i WHERE path LIKE '%s' AND hash IS NOT NULL",rel+"/%");
		
		String query = String.format("SELECT a FROM ImageAttribute a WHERE a.image IN (%s) AND a.value LIKE '%s'",imageQuery,'%'+search+'%');
		List<ImageAttribute> attributes = ImageAttribute.find(query).fetch();
		
		List<HashMap<String,Object>> out = new LinkedList<HashMap<String,Object>>();
		for(ImageAttribute attr : attributes) {
			HashMap<String,Object> entry = new HashMap<String,Object>();
			entry.put("image", attr.image.getStringPath());
			entry.put("attribute", attr);
			entry.put("id", attr.id);
			entry.put("before", attr.value);
			String result = attr.value.replaceAll(search, replace);
			String beforePreview = attr.value.replaceAll(search, "<b>"+search+"</b>");
			String afterPreview = attr.value.replaceAll(search, "<b>"+replace+"</b>");
			entry.put("beforePreview", beforePreview);
			entry.put("afterPreview", afterPreview);
			if (confirmReplace) {
				attr.updateAttribute(Security.getUser(), result, "[Updated by search/replace]");
			}
			entry.put("after", result);
			out.add(entry);
		}
		
		renderJSON(out);
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
		
		if (attribute.linkedAttribute != null) {
			attribute.linkedAttribute.linkedAttribute = null;
			attribute.linkedAttribute.save();
			attribute.linkedAttribute = null;
			attribute.save();
		}
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
				
				existingMap.remove(attr.attribute);
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