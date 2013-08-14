// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.ImageAttribute;
import models.ImageSetMembership;
import models.Paper;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.User;

public class DatabaseImageService {
	public static List<ImageAttribute> attributesForImageAndMode(Project project, DatabaseImage image, Boolean dataMode) {
		if (dataMode == null) {
			List<ImageAttribute> attributes = ImageAttribute.find("byProjectAndImage", project, image).fetch();
			return attributes;
		} else {
			List<ImageAttribute> attributes = ImageAttribute.find("byProjectAndImageAndData", project, image, dataMode).fetch();
			return attributes;
		}
	}
	
	public static Map<String,List<ImageAttribute>> attributeMapForImageAndMode(Project project, DatabaseImage image, Boolean dataMode) {
		List<ImageAttribute> attributes = attributesForImageAndMode(project, image, dataMode);
		HashMap<String,List<ImageAttribute>> map = new HashMap<String, List<ImageAttribute>>();
		
		for (ImageAttribute attr : attributes) {
			if (!map.containsKey(attr.attribute)) map.put(attr.attribute, new LinkedList<ImageAttribute>());
			
			List<ImageAttribute> list = map.get(attr.attribute);
			list.add(attr);
		}
		
		return map;
	}
}
