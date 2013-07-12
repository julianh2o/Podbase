// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.nio.file.Path;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import services.PathService;

@Entity
public class TemplateAssignment extends TimestampModel {
	public String path;
	
	@OneToOne
	public Project project;
	
	@OneToOne
	public Template template;

	public TemplateAssignment(Path path, Project project, Template template) {
		super();
		this.path = PathService.getRelativeString(path);
		this.project = project;
		this.template = template;
	}
	
	public static TemplateAssignment forPath(Project project, Path path) {
		TemplateAssignment assignment = TemplateAssignment.find("project = ? AND path = ?", project, PathService.getRelativeString(path)).first();
		return assignment;
	}
}
