package controllers;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import play.mvc.Util;
import play.mvc.With;
import services.PathService;
import services.SearchService;

import models.DatabaseImage;
import models.Project;
import models.Template;
import models.TemplateAssignment;
import models.TemplateAttribute;

@With(Security.class)
public class SearchController extends ParentController {
    public static void index(String query) {
    	Set<DatabaseImage> results = SearchService.performSimpleSearch(query);
    	render(results);
    }
    
    public static void doSearch(String query) {
    	Set<DatabaseImage> results = SearchService.performSimpleSearch(query);
    	renderJSON(results);
    }
}
