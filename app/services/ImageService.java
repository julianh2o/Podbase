package services;

import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

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

public class ImageService {
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

	public static BufferedImage appendImages(BufferedImage a, BufferedImage b) {
		int width = a.getWidth();
		int height = a.getHeight() + b.getHeight();
		
		BufferedImage full = new BufferedImage(width,height,a.getType());
		Graphics g = full.getGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect(0,0,full.getWidth(),full.getHeight());
		
		g.drawImage(a, 0, 0, null);
		g.drawImage(b, 0, a.getHeight(),null);
		
		return full;
	}

	public static BufferedImage adjustImage(BufferedImage image, double brightness, double contrast) {
		ImagePlus img = new ImagePlus("image",image);
		double range = 255;
		double center = 128;
		range = range / contrast;
		center = center - brightness;
		double min = center-range/2.0;
		double max = center+range/2.0;
		img.setDisplayRange(min,max);
		return img.getBufferedImage();
	}

}
