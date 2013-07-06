package controllers;

import java.nio.file.Path;
import java.util.List;

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
	}
	
	public static void duplicateTemplate(Template template, String newName) {
		Template newTemplate = new Template(template);
		newTemplate.name = newName;
		newTemplate.save();
	}
	
	public static void renameTemplate(Template template, String newName) {
		template.name = newName;
		template.save();
	}
	
	//TODO clean me up
	public static void setFolderTemplate(Project project, Template template, Path path) {
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, PathService.getRelativeString(path)).first();
		if (assignment == null) {
			assignment = new TemplateAssignment(path, project, template).save();
		} else {
			if (template == null) {
				assignment.delete();
				ok();
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
		
		ok();
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
			ok();
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
		ok();
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
