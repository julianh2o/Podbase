// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import play.mvc.Util;
import play.mvc.With;
import services.PathService;
import services.PermissionService;
import services.SearchService;

import models.DatabaseImage;
import models.Project;
import models.Template;
import models.TemplateAssignment;
import models.TemplateAttribute;
import models.User;

@With(Security.class)
public class SearchController extends ParentController {
    public static void doSearch(Project project, String query) throws Exception {
    	Set<DatabaseImage> results = SearchService.performDatabaseSearch(query,project);
    	System.out.println("Results: "+results.size());
    	results = filterSearchResults(project, results);
    	System.out.println("Filtered: "+results.size());
    	renderJSON(results);
    }
    
    @Util
    public static Set<DatabaseImage> filterSearchResults(Project project,Set<DatabaseImage> results) {
    	Set<DatabaseImage> filtered = new HashSet<DatabaseImage>();
    	for (DatabaseImage image : results) {
	    	User user = Security.getUser();
	    	if (PermissionService.userCanAccessPath(user,image.getPath()) && (project == null || PathService.isPathInProject(image.getPath(),project))) {
	    		filtered.add(image);
	    	}
    	}
    	return filtered;
    }
}
