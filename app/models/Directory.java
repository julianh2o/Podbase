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
public class Directory extends TimestampModel {
	@GsonTransient
	@OneToOne
	public Project project;
	
	public String path;

	public Directory(Project project, String path) {
		super();
		this.project = project;
		this.path = path;
	}
	
	public Path getPath() {
		return PathService.resolve(path);
	}
}
