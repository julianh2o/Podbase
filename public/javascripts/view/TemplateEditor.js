define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'text!tmpl/TemplateEditor.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(opts) {
				this.project = opts.project;
				this.setTemplate(opts.template);
			},
			
			setTemplate : function(template) {
				this.editTemplate = template;
				this.refresh();
			},
			
			refresh : function() {
				if (!this.editTemplate) {
					$(this.el).empty();
					return;
				}
				
				function sortAttributes(a,b) {
					return a.sort - b.sort
				}
				this.editTemplate.attributes = this.editTemplate.attributes.sort(sortAttributes);
				
				this.model = {template:this.editTemplate};
				this.render();
				
				$(".template-entry input",this.el).on("blur",$.proxy(this.onFieldBlur,this));
				$(".template-entry input[type=checkbox]",this.el).click($.proxy(this.onFieldBlur,this));
				
				$(".delete-template",this.el).click($.proxy(this.deleteTemplateClicked,this));
				
				$(".remove",this.el).on("click",$.proxy(this.onDeleteClicked,this));
				$(".add",this.el).on("click",$.proxy(this.addAttribute,this));
				
				var $container = $(".template-editor",this.el);
				$container.sortable({ handle: '.handle',stop: $.proxy(this.onSortStop,this)});
			},
			
			deleteTemplateClicked : function(e) {
				e.preventDefault();
				
				var yes = confirm("Really delete " + this.editTemplate.name + "?");
				if (yes) {
					this.deleteTemplate();
				}
			},
			
			deleteTemplate : function() {
				Link.deleteTemplate(this.editTemplate.id).post($.proxy(this.templateDeleted,this));
			},
			
			templateDeleted : function() {
				Link.getTemplates(this.project.id).pull();
			},
			
			returnStatus : function(response) {
				
			},
			
			addAttribute : function() {
				Link.addAttribute({templateId:this.editTemplate.id,name:""}).post($.proxy(this.handleNewAttribute,this));
			},
			
			handleNewAttribute : function(attribute) {
				var attrs = this.editTemplate.attributes.push(attribute);
				this.refresh();
			},
			
			getAttributeForElement : function($row) {
				return this.editTemplate.attributes[$row.data("index")];
			},
			
			onFieldBlur : function(event) {
				var $row = $(event.target).closest(".template-entry");
				
				var attr = this.getAttributeForElement($row);
				
				attr.name = $row.find("[name=name]").val();
				attr.hidden = $row.find("[name=hidden]").prop("checked");
				
				Link.updateAttribute({attributeId:attr.id,name:attr.name,hidden:attr.hidden}).post($.proxy(this.returnStatus,this));
			},
			
			onSortStop : function(event,ui) {
				var ids = Array();
				var entries = $(".template-entry",this.el);
				var self = this;
				_.each(entries,function(entry) {
					var attr = self.getAttributeForElement($(entry));
					ids[ids.length] = attr.id;
				});
				Link.updateAttributeOrder({templateId:this.editTemplate.id,ids:ids}).post($.proxy(this.returnStatus,this));
			},
			
			onDeleteClicked : function(event) {
				var $el = $(event.target).closest(".template-entry");
				var attribute = this.getAttributeForElement($el);
				
				var self = this;
				Link.removeAttribute({attributeId:attribute.id}).post(function() {
					self.editTemplate.attributes.splice($el.data("index"),1);
					self.refresh();
				});
			},
			
			renderTemplate : function(template) {
			},
			
			
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
