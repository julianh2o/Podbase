package controllers;

import java.util.List;

import models.Project;
import models.Template;
import models.TemplateAssignment;
import models.TemplateAttribute;

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
	}
	
	public static void setFolderTemplate(Project project, Template template, String path) {
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, path).first();
		if (assignment == null) {
			assignment = new TemplateAssignment(path, project, template);
		} else {
			assignment.template = template;
		}
		
		assignment.save();
		renderJSON(assignment);
	}
	
	public static void getTemplateForPath(Project project, String path) {
		TemplateAssignment assignment = TemplateAssignment.forPath(project, path);
		if (assignment == null) {
			ok();
		}
		
		renderJSON(assignment);
	}
	
	//TODO access check template/project
	public static void getTemplate(Template template) {
		renderJSON(template);
	}
	
	public static void addAttribute(Template template, String name) {
		TemplateAttribute attr = template.addAttribute(name);
		renderJSON(attr);
	}
	
	public static void updateAttribute(TemplateAttribute attribute, String name) {
		attribute.name = name;
		attribute.save();
		renderJSON(attribute);
	}

	public static void removeAttribute(TemplateAttribute attribute) {
		attribute.delete();
		ok();
	}
	
	public static void updateAttributeOrder(Template template, long[] ids) {
		int i = 0;
		for (long id : ids) {
			TemplateAttribute attribute = TemplateAttribute.findById(id);
			if (attribute.template == template) {
				attribute.sort = i++;
			}
			attribute.save();
		}
	}
}
