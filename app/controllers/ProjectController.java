package controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import access.Access;
import access.AccessType;
import access.ProjectAccess;

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
    
    @ProjectAccess(AccessType.VISIBLE)
    public static void getProject(Project project) {
    	renderJSON(project);
    }
    
    @Access(AccessType.CREATE_PROJECT)
    public static void createProject(String name) {
    	ProjectService.createProject(name);
    	ok();
    }
    
    @ProjectAccess(AccessType.EDITOR)
    public static void setDataMode(Project project, boolean dataMode) {
    	project.dataMode = dataMode;
    	ok();
    }
    
    @Access(AccessType.DELETE_PROJECT)
    public static void deleteProject(Project project) {
    	project.delete();
    	ok();
    }
}
