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
import org.elasticsearch.index.query.QueryBuilders;

import controllers.ParentController;

import access.AccessType;

import play.modules.elasticsearch.ElasticSearch;
import play.modules.elasticsearch.search.SearchResults;
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
	public static Set<DatabaseImage> performSimpleSearch(String query) {
		Set<DatabaseImage> results = new HashSet<DatabaseImage>();
		
		//List<ImageAttribute> found = Search.search("value:"+query, ImageAttribute.class).fetch();
		SearchResults<ImageAttribute> found = ElasticSearch.search(QueryBuilders.fieldQuery("value", query), ImageAttribute.class);

		for (ImageAttribute attr : found.objects) {
			results.add(attr.image);
		}
		
		return results;
	}

}
