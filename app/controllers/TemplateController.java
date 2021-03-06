// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import play.mvc.Util;
import play.mvc.With;
import services.PathService;
import services.YamlService;

import models.Project;
import models.Template;
import models.TemplateAssignment;
import models.TemplateAttribute;

@With(Security.class)
public class TemplateController extends ParentController {
	
	public static void getTemplates(Project project) {
		//TODO access check template/project
		List<Template> templates;
		
		if (project != null) {
			templates = project.templates;
		} else {
			templates = Template.findAll();
		}
		
		renderJSON(templates);
	}
	
	public static void addTemplate(Project project, String templateName) {
		Template template = new Template(project,templateName);
		project.templates.add(template);
		template.save();
		template.addAttribute("","");
	}
	
	public static void deleteTemplate(Template template) {
		template.delete();
		jsonOk();
	}
	
	public static void duplicateTemplate(Template template, String newName) {
		Template newTemplate = new Template(template);
		newTemplate.name = newName;
		newTemplate.save();
		jsonOk();
	}
	
	public static void renameTemplate(Template template, String newName) {
		template.name = newName;
		template.save();
		jsonOk();
	}
	
	public static void exportTemplate(Template template) {
		response.setHeader("Content-Disposition", "attachment; filename="+template.name+".template");
		renderJSON(template);
	}
	
	public static void importTemplate(Project project, File file) throws IOException {
		String data = FileUtils.readFileToString(file);
		Template importedTemplate = new Gson().fromJson(data, Template.class);
		
		Template newTemplate = project.addTemplate(importedTemplate.name);
		for (TemplateAttribute attr : importedTemplate.attributes) {
			TemplateAttribute newAttr = newTemplate.addAttribute(attr.name, attr.description, attr.hidden);
			newAttr.sort = attr.sort;
			newAttr.save();
		}
		
		jsonOk();
	}
	
	//TODO clean me up
	public static void setFolderTemplate(Project project, Template template, Path path) {
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, PathService.getRelativeString(path)).first();
		if (assignment == null) {
			assignment = new TemplateAssignment(path, project, template).save();
		} else {
			if (template == null) {
				assignment.delete();
				jsonOk();
				return;
			}
			
			assignment.template = template;
		}
		
		assignment.save();
		renderJSON(assignment);
	}
	
	public static void clearFolderTemplate(Project project, Path path) {
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, PathService.getRelativeString(path)).first();
		
		if (assignment != null) assignment.delete();
		
		jsonOk();
	}
	
	//TODO move to template service
	@Util
	public static TemplateAssignment templateForPath(Project project, Path path) {
		Path cpath = path;
		while(true) {
			
			TemplateAssignment assignment = TemplateAssignment.forPath(project, cpath);
			if (assignment != null) return assignment;
			
			if (PathService.getRelativeString(cpath).equals("/")) {
				return null;
			}
			
			cpath = cpath.getParent();
		}
	}
	
	public static void getTemplateForPath(Project project, Path path) {
		TemplateAssignment assignment = templateForPath(project,path);
			
		if (assignment == null) {
			jsonOk();
		}
		
		renderJSON(assignment);
	}
	
	//TODO access check template/project
	public static void getTemplate(Template template) {
		renderJSON(template);
	}
	
	public static void addAttribute(Template template, String name, String description) {
		TemplateAttribute attr = template.addAttribute(name,description);
		renderJSON(attr);
	}
	
	public static void updateAttribute(TemplateAttribute attribute, String name, String description, boolean hidden) {
		attribute.name = name;
		attribute.description = description;
		attribute.hidden = hidden;
		attribute.save();
		renderJSON(attribute);
	}

	public static void removeAttribute(TemplateAttribute attribute) {
		attribute.delete();
		jsonOk();
	}
	
	public static void updateAttributeOrder(Template template, String ids) {
		int i = 0;
		for (String strid : ids.split(",")) {
			long id = Long.parseLong(strid);
			TemplateAttribute attribute = TemplateAttribute.findById(id);
			if (attribute.template == template) {
				attribute.sort = i++;
			}
			attribute.save();
		}
	}
	
	public static void downloadTemplates() {
		List<Template> templates = Template.all().fetch();
		response.setHeader("Content-Disposition", "attachment; filename=templates.yml");
		renderText(YamlService.toYaml(templates));
	}
}
