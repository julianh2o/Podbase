package controllers;

import com.google.gson.Gson;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import util.PodbaseUtil;

public class ParentController extends Controller {
    @Before
    static void setPageAttributes() {
	   renderArgs.put("current_user", Security.isConnected()?Security.connected():null);
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
	public static void debug(Object o) {
		StackTraceElement frame = new Exception().getStackTrace()[0];
		System.out.println("["+frame.getFileName() + ":"+ frame.getLineNumber()+"] "+o);
	}
}
