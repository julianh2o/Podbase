package jobs;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import controllers.ImageBrowser;

import access.AccessType;

import play.*;
import play.jobs.*;
import play.modules.search.Search;
import play.test.*;
import services.PathService;
import services.PermissionService;
 
import models.*;
 
//@Every("1h")
public class SearchIndexMaintenance extends Job {
	
    public void doJob() throws Exception {
        Search.rebuildAllIndexes();
    }
}
