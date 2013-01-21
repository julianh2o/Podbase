package util;

import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import models.DatabaseImage;
import models.GsonTransient;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import controllers.ImageBrowser;

public class PodbaseUtil {
	public static boolean isImage(File file) {
		String[] extensions = {"jpg","jpeg","png","tiff","tif","gif","bmp"};
		
		String name = file.getName().toLowerCase();
		for (String ext : extensions) {
			if (name.endsWith(ext)) return true;
		}
		return false;
	}
	
	public static String concatenatePaths(String path, String rel) {
		String sep = "/";
		if (path.endsWith("/") || rel.startsWith("/")) sep = "";
		return path + sep + rel;
	}
	
	public static DatabaseImage imageForFile(File f) {
        Path pathAbsolute = f.toPath();
        Path pathBase = ImageBrowser.getRootImageDirectoryFile().toPath();
        Path pathRelative = pathBase.relativize(pathAbsolute);
        String path = "/"+pathRelative.toString();
        return DatabaseImage.forPath(path);
	}
	
	public static boolean isValidPath(String path) {
		if (!path.startsWith("/")) return false;
		if (path.length() == 1) return true;
		if (path.endsWith("/")) return false;
		
		return true;
	}
	
	public static void assertPath(String path) {
		if (!isValidPath(path)) throw new RuntimeException("Invalid path: "+path);
	}
	
	// Scales the given buffered image to fit within the box given
	public static BufferedImage scaleImageToFit(BufferedImage image, Integer targetWidth, Integer targetHeight) {
		if (targetWidth == null && targetHeight == null) return image;

		int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();

		Integer dw = targetWidth  == null ? null : originalWidth - targetWidth;
		Integer dh = targetHeight == null ? null : originalHeight - targetHeight;

		double aspect = (double)originalWidth / (double)originalHeight;

		int height;
		int width;
		if (targetHeight != null && (targetWidth == null || dw < dh)) {
			height = targetHeight;
			width = (int)(aspect * height);
		} else {
			width = targetWidth;
			height = (int)(width / aspect);
		}

		return scaleImage(image,width,height);
	}

	public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics2D = scaled.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, width, height, null);

		graphics2D.dispose();

		return scaled;
	}

	public static BufferedImage makeHistogram(BufferedImage source, int width, int height) {
		BufferedImage hist = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = hist.getGraphics();

		ImagePlus img = new ImagePlus("image",source);
		ImageStatistics stats = img.getStatistics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,hist.getWidth(),hist.getHeight());
		int cpix = 0;
		double cellsPerPix = (double)256/(double)hist.getWidth();
		for (int i=0; i<hist.getWidth(); i++) {
			int tot = 0;
			int count = 0;
			while(cpix <= i*cellsPerPix) {
				tot = tot + stats.histogram[cpix];
				count ++;
				cpix++;
			}
			if (count != 0) {
				tot = tot/count;
			}
			
			
			double percent = (double)tot/(double)stats.maxCount;
			int barHeight = (int)(percent*hist.getHeight());
			g.setColor(Color.BLACK);
			g.drawLine(i,hist.getHeight()-barHeight,i,hist.getHeight());
			
		}

		return hist;
	}

	public static Gson getGsonExcludesGsonTransient() {
		Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipClass(Class<?> arg0) {
				return false;
			}

			@Override
			public boolean shouldSkipField(FieldAttributes field) {
				GsonTransient annotation = field.getAnnotation(GsonTransient.class);
				return annotation != null;
			}}).create();
		return gson;
	}

}
