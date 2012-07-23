new function($) {
	Array.prototype.remove = function(object) {
		for (var i=0; i<this.length; i++) {
			if (this[i] == object) {
				this.splice(i,1); 
				return;
			}
		}
	};
	
	this.project = null;
	this.template = null;
	
	var that = this;
	this.setProject = function(newproject) {
		that.project = newproject;
		that.renderTemplateList();
	}
	
	this.renderTemplateList = function() {
		var templates = this.project.templates;
		$.each(templates,function() {
			var entry = $("<li />").append("<a href='#' />");
			var a = entry.find("a");
			a.html(this.name);
			a.bind("click",{template:this},that.handleTemplateClick);
			
			$("#templates .body").append(entry);
		});
	}
	
	this.handleTemplateClick = function(event) {
		that.template = event.data.template;
		that.renderTemplate(that.template);
	}
	
	this.returnStatus = function(response) {
		
	}
	
	this.addAttribute = function() {
		$.post("@{TemplateController.addAttribute()}",{templateId:that.template.id,name:""},that.handleNewAttribute,'json');
	}
	
	this.handleNewAttribute = function(attribute) {
		var attrs = that.template.attributes;
		attrs[attrs.length] = attribute;
		that.renderTemplate(that.template);
	}
	
	this.findAttribute = function(id) {
		var attributes = this.template.attributes;
		for (var i=0; i<attributes.length; i++) {
			var attr = attributes[i];
			if (attr.id == id) return attr;
		}
		return null;
	}
	
	this.handleBlur = function(event) {
		var id = event.data.attribute.id;
		var text = event.currentTarget.value;
		
		var attribute = that.findAttribute(id);
		attribute.name = text;
		
		$.post("@{TemplateController.updateAttribute()}",{id:id,name:text},that.returnStatus,'json');
	}
	
	this.handleSortChange = function(event,ui) {
		var ids = Array();
		var entries = $("#templateinfo .body .entry");
		entries.each(function() {
			ids[ids.length] = $(this).data("template").id;
		});
		$.post("@{TemplateController.updateAttributeOrder()}",{templateId:that.template.id,ids:ids},that.returnStatus,'json');
	}
	
	this.handleRemoveAttribute = function(event) {
		var entry = $(event.currentTarget).parents(".entry");
		var attribute = event.data.attribute;
		$.post("@{TemplateController.removeAttribute()}",{id:attribute.id},new function(a,b,c) {
			that.template.attributes.remove(attribute);
			entry.remove();
		},'json');
	}
	
	this.renderTemplate = function(template) {
		var container = $("#attributes");
		container.empty();
		var attributes = template.attributes;
		
		function sortAttributes(a,b) {
			return a.sort - b.sort
		}
		
		function makeEntry() {
			var entry = $("<div class='entry ui-state-default ui-corner-all' />")
			entry.append("<div class='cell'><span class='handle ui-icon ui-icon-arrowthick-2-n-s'></span></div>")
			entry.append("<div class='cell'><input type='text' name='name' class='seamless' /></div>");
			entry.append("<div class='cell'><span class='remove ui-icon ui-icon-closethick'></div>");
			return entry;
		}
		
		attributes = attributes.sort(sortAttributes);
		$.each(template.attributes,function() {
			var entry = makeEntry();
			entry.data("template",this);
			var name = entry.find("input[name='name']").val(this.name);
			name.bind("blur",{attribute:this,element:name},that.handleBlur);
			entry.find(".remove").bind("click",{attribute:this},that.handleRemoveAttribute);
			
			container.append(entry);
		});
		$("#addattribute").show();
		container.sortable({ handle: '.handle',stop: this.handleSortChange});
		container.disableSelection();
	}
	
	$(document).ready(function() {
		$.post("@{ProjectController.getProject()}",{projectId:projectId},that.setProject,'json');
		$("#addattribute a").click(that.addAttribute);
	});
}(jQuery);