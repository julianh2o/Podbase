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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static Set<DatabaseImage> performLuceneSearch(String value, Project project) throws Exception {
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
	
	public static Set<DatabaseImage> performDatabaseSearch(String query, Project project) throws Exception {
		List<String> tokens = tokenizeQuery(query);
		int imageAttributes = 0;
		String imageAttributeList = "";
		String queryBody = "";
		String pathQuery = "";
		for (String tok : tokens) {
			imageAttributeList+=", ImageAttribute a"+imageAttributes;
			queryBody += " AND a"+imageAttributes+".image.id=d.id";
			queryBody += " AND a"+imageAttributes+".project.id="+project.id;
			
			if (tok.indexOf('=') != -1) {
				String[] split = tok.split("=");
				queryBody += " AND a"+imageAttributes+".attribute='"+split[0]+"'";
				queryBody += " AND a"+imageAttributes+".value LIKE '%"+split[1]+"%'";
			} else {
				queryBody += " AND a"+imageAttributes+".value LIKE '%"+tok+"%'";
			}
			pathQuery += " AND d.path LIKE '%"+tok+"%'";
			imageAttributes++;
		}
		
		queryBody = queryBody.substring(5);
		pathQuery = pathQuery.substring(5);
		String jql = String.format("SELECT d from DatabaseImage d%s WHERE %s",imageAttributeList,queryBody);
		System.out.println(jql);
		List<DatabaseImage> queryResults = DatabaseImage.find(jql).fetch(20);
		Set<DatabaseImage> results = new HashSet<DatabaseImage>(queryResults);
		jql = String.format("SELECT d from DatabaseImage d WHERE %s",pathQuery);
		System.out.println(jql);
		queryResults = DatabaseImage.find(jql).fetch(20);
		results.addAll(queryResults);
		return results;
	}
	
	private static List<String> tokenizeQuery(String query) {
		List<String> tokens = new LinkedList<String>();
		
		int i = 0;
		StringBuilder sb = new StringBuilder();
		boolean inQuote = false;
		while(i < query.length()) {
			char c = query.charAt(i);
			if (inQuote) {
				if (c == '"') {
					inQuote = false;
					tokens.add(sb.toString().trim());
					sb = new StringBuilder();
				} else {
					sb.append(c);
				}
			} else {
				if (c == '"') {
					inQuote = true;
				} else if (c == ' ') {
					tokens.add(sb.toString().trim());
					sb = new StringBuilder();
				} else {
					sb.append(c);
				}
			}
			i++;
		}
		if (sb.toString().trim().length() != 0) tokens.add(sb.toString().trim());
		
		return tokens;
	}

	public static Set<DatabaseImage> performSimpleDatabaseSearch(String value, Project project) throws Exception {
		Set<DatabaseImage> results = new HashSet<DatabaseImage>();
		
		if (value.trim().length() == 0) return results;
		
		List<ImageAttribute> found = ImageAttribute.find("byValueLike","%"+value+"%").fetch();
		//List<ImageAttribute> found = ImageAttribute.find("byProjectAndValueLike",project,"%"+value+"%").fetch();
		List<DatabaseImage> imageResults = DatabaseImage.find("byPathLike","%"+value+"%").fetch(); 

		for (DatabaseImage image : imageResults) {
			results.add(image);
		}
		
		for (ImageAttribute attr : found) {
			results.add(attr.image);
		}
		
		return results;
	}
}
