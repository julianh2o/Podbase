package util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class PodbaseUtil {
	public static String concatenatePaths(String path, String rel) {
		String sep = "/";
		if (path.endsWith("/") || rel.startsWith("/")) sep = "";
		return path + sep + rel;
	}
	
	// Scales the given buffered image to fit within the box given
	public static BufferedImage scaleImageToFit(BufferedImage image, int targetWidth, int targetHeight) {
		int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();
		
//		System.out.println("orig_width: "+originalWidth);
//		System.out.println("orig_height: "+originalHeight);
		
		int dw = originalWidth - targetWidth;
		int dh = originalHeight - targetHeight;
		
//		System.out.println("delta_width: "+dw);
//		System.out.println("delta_height: "+dh);
		
		double aspect = (double)originalWidth / (double)originalHeight;
		
//		System.out.println("aspect ratio: "+aspect);
		
		int height;
		int width;
		if (dw < dh) {
//			System.out.println("case 1");
			height = targetHeight;
			width = (int)(aspect * height);
		} else {
//			System.out.println("case 2");
			width = targetWidth;
			height = (int)(width / aspect);
		}
		
//		System.out.println("final width: "+width);
//		System.out.println("final height: "+height);
		
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics2D = scaled.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, width, height, null);

		graphics2D.dispose();
	
		return scaled;
	}
}
