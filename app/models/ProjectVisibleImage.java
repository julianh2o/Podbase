// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.UniqueConstraint;

import services.PathService;
import util.PodbaseUtil;

@Entity
public class ProjectVisibleImage extends TimestampModel {
	@OneToOne
	public Project project;
	
	@OneToOne
	public DatabaseImage image;

	public ProjectVisibleImage(Project project, DatabaseImage image) {
		super();
		this.project = project;
		this.image = image;
	}

	public static ProjectVisibleImage get(Project project, DatabaseImage image) {
		return find("byProjectAndImage", project, image).first();
	}
	
	public static List<ProjectVisibleImage> getVisibleImages(Project project) {
		return find("byProject", project).fetch();
	}
	
	public static Set<Path> getVisibleDirectories(Project project) {
		HashSet<Path> visibleDirectories = new HashSet<Path>();
		List<ProjectVisibleImage> visibleImages = ProjectVisibleImage.getVisibleImages(project);
		
		for (ProjectVisibleImage vi : visibleImages) {
			Path path = vi.image.getPath();
			visibleDirectories.addAll(PathService.getParentDirectories(path));
		}
		
		return visibleDirectories;
	}

	public static ProjectVisibleImage setVisible(Project project, DatabaseImage image, boolean visible) {
		ProjectVisibleImage pvi = get(project,image);
		if (pvi == null && visible) {
			return new ProjectVisibleImage(project,image).save();
		} else if (pvi != null && !visible) {
			pvi.delete();
			return null;
		}
		return pvi;
	}
}
