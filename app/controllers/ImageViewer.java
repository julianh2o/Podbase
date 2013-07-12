// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import play.mvc.With;
import services.PathService;

@With(Security.class)
public class ImageViewer extends ParentController {
	public static void index(Path path) {
		ImagePlus image = ImageBrowser.getImage(path);
		int width = image.getWidth();
		int height = image.getHeight();
		
		String relativePath = PathService.getRelativeString(path);
		render(relativePath,width,height);
	}
	
	public static void script() {
		renderTemplate("ImageViewer/script.js");
	}
}
