package services;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

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
	public static ImagePlus scaleImageToFit(ImagePlus image, Integer targetWidth, Integer targetHeight) {
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

	public static ImagePlus scaleImage(ImagePlus image, int width, int height) {
		ImageProcessor ip = image.getProcessor();
		
		ip.setInterpolate(true);
		ImageProcessor ip2 = ip.resize(width,height);
		
		return new ImagePlus(image.getTitle(),ip2);
	}

	public static BufferedImage makeHistogram(ImagePlus source, int width, int height) {
		BufferedImage hist = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = hist.getGraphics();

		ImageStatistics stats = source.getStatistics();
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
	
	public static BufferedImage addWatermark(BufferedImage a) {
		Graphics g = a.createGraphics();
		
		BufferedImage watermark = null;
		try {
			watermark = ImageIO.read(new File("./public/images/logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
			return a;
		}		
		
		int width = (int)(a.getWidth()*.3);
		double aspect = (double)watermark.getWidth() / (double)watermark.getHeight();
		
		int height = (int)(1.0 / (aspect / width));
		
		int xSpace = (int)(width*0.1);
		int ySpace = (int)(width*0.1);
		
		g.drawImage(watermark, a.getWidth() - width - xSpace, a.getHeight() - height - ySpace, width, height, null);
		
		return a;
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

	// Brightness between -150 and 150
	// Contrast between -1 and infinity
	public static ImagePlus adjustImage(ImagePlus image, double brightness, double contrast) {
		ImageProcessor ip = image.getProcessor();
	
		contrast = Math.pow(2, contrast/30) - 1;
		
		brightness = Math.min(150, Math.max(-150,brightness));
		contrast = Math.max(0,contrast+1);
		
		double mul = (1 + brightness / 150.0) * contrast;
		double add = -128*contrast + 128;
		
		//byte[], short[], float[] or int[]
		Object pixels = ip.getPixels();
		if (pixels instanceof byte[]) {
			byte[] data = (byte[])pixels;
			
			for (int i=0; i<data.length; i++) {
				int pixel = (int)data[i] + 128;
				pixel = (int)(pixel * mul + add);
				data[i] = (byte)(Math.min(255, Math.max(0,pixel))-128);
			}
		} else if (pixels instanceof short[]) {
			System.out.println("short - THIS PROBABLY DOESNT WORK");
			short[] data = (short[])pixels;
			
			for (int i=0; i<data.length; i++) {
				int pixel = (int)data[i] + 128;
				pixel = (int)(pixel * mul + add);
				if (i<30) System.out.print(pixel+" ");
				data[i] = (short)(Math.min(255, Math.max(0,pixel))-128);
			}
		} else if (pixels instanceof float[]) {
			System.out.println("float - THIS PROBABLY DOESNT WORK");
			float[] data = (float[])pixels;
			
			for (int i=0; i<data.length; i++) {
				int pixel = (int)(data[i] * mul + add);
				data[i] = (float)Math.min(255, Math.max(0,pixel));
			}
		} else if (pixels instanceof int[]) {
			int[] data = (int[])pixels;
			
			for (int i=0; i<data.length; i++) {
	            int a = (data[i] >> 24) & 0xff;
	            int r = (data[i] >> 16) & 0xff;
	            int g = (data[i] >> 8) & 0xff;
	            int b = data[i] & 0xff;
	            
				a = (int)(a * mul + add);
				r = (int)(r * mul + add);
				g = (int)(g * mul + add);
				b = (int)(b * mul + add);
				
				a = Math.min(255, Math.max(0,a));
				r = Math.min(255, Math.max(0,r));
				g = Math.min(255, Math.max(0,g));
				b = Math.min(255, Math.max(0,b));
				
				data[i] = (a<<24) | (r<<16) | (g<<8) | b;  
			}
		}
		
		return image;
	}

}
