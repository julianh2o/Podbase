package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class TemplateAttribute extends TimestampModel {
	public String name;
	public String type;
	public Integer sort;
	
	@GsonTransient
	@OneToOne
	public Template template;
	
	public TemplateAttribute(Template template, String name) {
		super();
		this.template = template;
		this.name = name;
		this.type = "normal";
		this.sort = template.attributes.size();
	}
}
