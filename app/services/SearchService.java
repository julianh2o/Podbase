// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package services;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;

import play.modules.search.Query;
import play.modules.search.Query.SearchException;
import play.modules.search.Search;
import play.mvc.Util;

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

public class SearchService {
	public static Set<DatabaseImage> performSimpleSearch(String value, Project project) throws Exception {
		try {
			Set<DatabaseImage> results = new HashSet<DatabaseImage>();
			
			if (value.trim().length() == 0) return results;
			
			String lucene = String.format("project:%d AND value:%s*",project.id.longValue(),value);
			Query query = Search.search(lucene, ImageAttribute.class);
			List<ImageAttribute> found = query.all().fetch();
			

			List<DatabaseImage> imageResults = DatabaseImage.find("byPathLike","%"+value+"%").fetch(); 

			for (DatabaseImage image : imageResults) {
				results.add(image);
			}
			
			for (ImageAttribute attr : found) {
				results.add(attr.image);
			}
			
			return results;
		} catch (SearchException se) {
			//Search.getCurrentStore().rebuild(ImageAttribute.class.getName());
			throw se;
		}
	}

}
