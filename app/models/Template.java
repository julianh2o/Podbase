package models;

import java.util.LinkedList;
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
		this.attributes = new LinkedList<TemplateAttribute>();
	}
	
	public Template(Template template) {
		this.project = template.project;
		this.name = template.name;
		this.attributes = new LinkedList<TemplateAttribute>();
		this.save();
		
		for (TemplateAttribute attribute : template.attributes) {
			addAttribute(attribute.name,attribute.description, attribute.hidden);
		}
	}
	
	public TemplateAttribute addAttribute(String name, String description) {
		return this.addAttribute(name, description,false);
	}

	public TemplateAttribute addAttribute(String name, String description, boolean hidden) {
		TemplateAttribute attr = new TemplateAttribute(this, name, description, hidden).save();
		this.attributes.add(attr);
		this.save();
		return attr;
	}
}
