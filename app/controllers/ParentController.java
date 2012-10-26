package controllers;

import com.google.gson.Gson;

import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import util.PodbaseUtil;

public class ParentController extends Controller {
    @Before
    static void setPageAttributes() {
		renderArgs.put("user", Security.getUser());
	    renderArgs.put("frameworkId", Play.id);
    }
	
	@Util
	protected static void renderJSON(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    renderJSON(gson.toJson(o));  
	}
	
	@Util
	protected static String renderJSONToString(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    return gson.toJson(o);
	}
	
	@Util
	public static void debug(Object... objects) {
		StackTraceElement frame = new Exception().getStackTrace()[0];
		StringBuffer sb = new StringBuffer();
		for (Object o : objects) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(o);
		}
		
		System.out.println("["+frame.getFileName() + ":"+ frame.getLineNumber()+"] "+sb.toString());
	}
}
