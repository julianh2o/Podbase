package controllers;

import java.util.List;

import models.Project;
import models.Template;
import models.TemplateAssignment;
import models.TemplateAttribute;

public class TemplateController extends ParentController {
	
	public static void getTemplates(Long projectId) {
		//TODO access check template/project
		List<Template> templates;
		
		if (projectId != null) {
			Project project = Project.findById(projectId);
			templates = project.templates;
		} else {
			templates = Template.findAll();
		}
		
		renderJSON(templates);
	}
	
	public static void setFolderTemplate(Long projectId, long templateId, String path) {
		Project project = null;
		if (projectId != null) project = Project.findById(projectId);
		
		Template template = Template.findById(templateId);
		
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, path).first();
		if (assignment == null) {
			assignment = new TemplateAssignment(path, project, template);
		} else {
			assignment.template = template;
		}
		
		assignment.save();
		debug(assignment);
		renderJSON(assignment);
	}
	
	public static void getTemplateForPath(Long projectId, String path) {
		Project project = Project.findById(projectId);
		TemplateAssignment assignment = TemplateAssignment.forPath(project, path);
		if (assignment == null) {
			ok();
		}
		
		renderJSON(assignment);
	}
	
	public static void getTemplate(long templateId) {
		//TODO access check template/project
		Template template = Template.findById(templateId);
		renderJSON(template);
	}
	
	public static void addAttribute(long templateId, String name) {
		//TODO access check template/project
		Template templateObject = Template.findById(templateId);
		TemplateAttribute attr = templateObject.addAttribute(name);
		renderJSON(attr);
	}

	public static void updateAttribute(long id, String name) {
		//TODO access check template/project
		TemplateAttribute attribute = TemplateAttribute.findById(id);
		attribute.name = name;
		attribute.save();
		renderJSON(attribute);
	}

	public static void removeAttribute(long id) {
		//TODO access check template/project
		TemplateAttribute attribute = TemplateAttribute.findById(id);
		attribute.delete();
		ok();
	}
	
	public static void updateAttributeOrder(long templateId, long[] ids) {
		Template template = Template.findById(templateId);
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
