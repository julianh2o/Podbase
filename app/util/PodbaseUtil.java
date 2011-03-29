package util;

public class PodbaseUtil {
	public static String concatenatePaths(String path, String rel) {
		String sep = "/";
		if (path.endsWith("/") || rel.startsWith("/")) sep = "";
		return path + sep + rel;
	}
}
