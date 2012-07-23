package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Template extends TimestampModel {
	@GsonTransient
	@OneToOne
	public Project project;
	
	public String name;
	
	@OneToMany(mappedBy="template", cascade=CascadeType.ALL)
	public List<TemplateAttribute> attributes;
	
	public Template(Project project, String name) {
		super();
		this.project = project;
		this.name = name;
	}

	public TemplateAttribute addAttribute(String name) {
		TemplateAttribute attr = new TemplateAttribute(this, name).save();
		this.attributes.add(attr);
		this.save();
		return attr;
	}
}
