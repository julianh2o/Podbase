package controllers;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import play.mvc.With;
import services.PathService;

@With(Security.class)
public class ImageViewer extends ParentController {
	public static void index(Path path) {
		BufferedImage image = ImageBrowser.getImage(path);
		int width = image.getWidth();
		int height = image.getHeight();
		
		render(path.toString(),width,height);
	}
	
	public static void script() {
		renderTemplate("ImageViewer/script.js");
	}
}
