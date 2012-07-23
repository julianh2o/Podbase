package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class TemplateManager extends ParentController {

    public static void index() {
    	List<Project> projects = Project.findAll();
    	render(projects);
    }
    
    public static void projectIndex(long projectId) {
    	Project project = Project.findById(projectId);
		renderTemplate("TemplateManager/index.html", project);
    }
    
    public static void listTemplates(long projectId) {
    	Project project = Project.findById(projectId);
    	List<Template> templates = project.templates;
    	render(project,templates);
    }
    
    public static void showTemplate(long projectId, long templateId) {
    	Project project = Project.findById(projectId);
    	Template template = Template.findById(templateId);
    	if (!template.project.equals(project)) error("This template does not belong to this project");
    	render(project,template);
    }
    
	public static void script() {
		renderTemplate("TemplateManager/script.js");
	}
	
	public static void addAttribute(long templateId, String name) {
		Template templateObject = Template.findById(templateId);
		TemplateAttribute attr = templateObject.addAttribute(name);
		renderJSON(attr);
	}
	
	public static void updateAttribute(long id, String name) {
		TemplateAttribute attribute = TemplateAttribute.findById(id);
		attribute.name = name;
		attribute.save();
		renderJSON(attribute);
	}
	
	public static void removeAttribute(long id) {
		TemplateAttribute attribute = TemplateAttribute.findById(id);
		attribute.delete();
		ok();
	}
	
}
