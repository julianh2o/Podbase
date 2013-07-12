// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
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
