package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

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
}
