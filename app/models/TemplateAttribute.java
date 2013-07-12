// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class TemplateAttribute extends TimestampModel {
	public String name;
	public String description;
	public boolean hidden;
	public String type;
	public Integer sort;
	
	@GsonTransient
	@OneToOne
	public Template template;
	
	public TemplateAttribute(Template template, String name, String description, boolean hidden) {
		super();
		this.template = template;
		this.name = name;
		this.description = description;
		this.hidden = hidden;
		this.type = "normal";
		this.sort = template.attributes.size();
	}
}
