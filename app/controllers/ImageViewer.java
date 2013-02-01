package controllers;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import play.mvc.With;
import services.PathService;

@With(Security.class)
public class ImageViewer extends ParentController {
	public static void index(String strPath) {
		Path path = PathService.resolve(strPath);
		
		BufferedImage image = ImageBrowser.getImage(path);
		int width = image.getWidth();
		int height = image.getHeight();
		
		render(strPath,width,height);
	}
	
	public static void script() {
		renderTemplate("ImageViewer/script.js");
	}
}
