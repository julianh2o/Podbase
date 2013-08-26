// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import access.Access;
import access.AccessType;
import access.ModelAccess;

import play.mvc.Util;
import play.mvc.With;
import services.PermissionService;
import services.ProjectService;

import models.Directory;
import models.Permission;
import models.Project;
import models.User;

@With(Security.class)
public class ProjectController extends ParentController {
    public static void getProjects() {
    	Set<Project> projects = ProjectService.getVisibleProjects();
    	renderJSON(projects);
    }
    
    @ModelAccess(AccessType.LISTED)
    public static void getProject(Project project) {
    	renderJSON(project);
    }
    
    @Access(AccessType.CREATE_PROJECT)
    public static void createProject(String name) {
    	ProjectService.createProject(name);
    	ok();
    }
    
    @ModelAccess(AccessType.SET_DATA_MODE)
    public static void setDataMode(Project project, boolean dataMode) {
    	project.dataMode = dataMode;
    	project.save();
    	ok();
    }
    
    @Access(AccessType.DELETE_PROJECT)
    public static void deleteProject(Project project) {
    	project.delete();
    	ok();
    }
}
