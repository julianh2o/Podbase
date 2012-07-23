package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class TemplateAssignment extends TimestampModel {
	public String path;
	
	@OneToOne
	public Project project;
	
	@OneToOne
	public Template template;

	public TemplateAssignment(String path, Project project, Template template) {
		super();
		this.path = path;
		this.project = project;
		this.template = template;
	}
	
	public static TemplateAssignment forPath(Project project, String path) {
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, path).first();
		return assignment;
	}
}
