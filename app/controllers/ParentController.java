package controllers;

import com.google.gson.Gson;

import play.mvc.Before;
import play.mvc.Controller;
import util.PodbaseUtil;

public class ParentController extends Controller {
    @Before
    static void setPageAttributes() {
	   renderArgs.put("current_user", Security.isConnected()?Security.connected():null);
    }
	
	protected static void renderJSON(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    renderJSON(gson.toJson(o));  
	}
	
	protected static String renderJSONToString(Object o) {
	    Gson gson = PodbaseUtil.getGsonExcludesGsonTransient();
	    return gson.toJson(o);
	}
	
	public static void debug(Object o) {
		StackTraceElement frame = new Exception().getStackTrace()[1];
		System.out.println("["+frame.getFileName() + ":"+ frame.getLineNumber()+"] "+o);
	}
}
