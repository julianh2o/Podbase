// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import play.Play;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Response;
import play.mvc.results.Forbidden;
import play.mvc.Util;
import util.PodbaseUtil;

public class ParentController extends Controller {
    private static Logger performance = Logger.getLogger("PERFORMANCE");
    	
    @Before
    static void setPageAttributes() {
		renderArgs.put("user", Security.getUser());
	    renderArgs.put("frameworkId", Play.id);
	    renderArgs.put("serverUrl", request.getBase());
	    renderArgs.put("serverTime", System.currentTimeMillis() / 1000L);
    }
    
    @Before
    static void startTimer() {
    	renderArgs.put("renderStart", System.currentTimeMillis());
    }
    
    @After
    static void stopTimer() {
    	long currentTime = System.currentTimeMillis();
    	long delta = currentTime - (Long)renderArgs.get("renderStart");
    	renderArgs.put("renderStop", currentTime);
    	renderArgs.put("renderTime", delta);
    	performance.info("Pagetimer: "+delta+" ms "+request.url);
    }
	
	@Util
	protected static void renderJSON(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    renderJSON(gson.toJson(o));  
	}
	
	protected static void jsonOk() {
		renderJSON("{\"status\":\"Ok\"}");
	}
	
	@Util
	protected static String renderJSONToString(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    return gson.toJson(o);
	}
	
	@Util
	public static void debug(Object... objects) {
		StackTraceElement frame = new Exception().getStackTrace()[1];
		StringBuffer sb = new StringBuffer();
		for (Object o : objects) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(o);
		}
		
		System.out.println("["+frame.getFileName() + ":"+ frame.getLineNumber()+"] "+sb.toString());
	}
	
	@Util
	public static void renderImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Response.current().contentType = "image/png";

		renderBinary(bais);
	}
}
