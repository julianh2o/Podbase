package controllers;

import java.awt.image.BufferedImage;

public class ImageViewer extends ParentController {
	public static void index(String path) {
		BufferedImage image = ImageBrowser.getImage(path);
		int width = image.getWidth();
		int height = image.getHeight();
		render(path,width,height);
	}
	
	public static void script() {
		renderTemplate("ImageViewer/script.js");
	}
}
