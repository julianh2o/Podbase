package util;

public class StringUtil {
	public static boolean isNullOrEmpty(String s) {
		if (s == null) return true;
		if ("".equals(s)) return true;
		return false;
	}
	
	public static boolean isNullOrEmptyOrWhitespace(String s) {
		if (isNullOrEmpty(s)) return true;
		if ("".equals(s.trim())) return true;
		return false;
	}
}
