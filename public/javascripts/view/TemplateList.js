define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'view/TemplateChooser', 'text!tmpl/TemplateList.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, TemplateChooser, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.project = this.options.project;
				this.link = Link.getTemplates(this.project.id);
				this.link.asap($.proxy(this.refresh,this))
			},
			
			reload : function() {
				Link.getTemplates(this.project.id).pull();
			},
			
			getSelectedTemplate : function() {
				return this.selected;
			},
			
			selectIndex : function(index) {
				var $templates = $(".template",this.el)
				if ($templates.length) {
					var $el = $templates.eq(index);
					this.templateSelected($el);
				}
			},
			
			refresh : function() {
				$(this).trigger("TemplateDeselected");
				
				this.model = {templates:Link.getTemplates(this.project.id).getData()};
				this.render();
				
				if (this.selected) this.templateSelected($("a[data-id="+this.selected.id+"]",this.el));
				
				$(".template",this.el).click($.proxy(this.templateClicked,this));
				$(".add",this.el).click($.proxy(this.addTemplateClicked,this));
				
				$(".template-upload",this.el).fileupload({
			        dataType: 'json',
			        formData: $.proxy(function() {
						return [{name:"project.id","value":this.project.id}];
			        },this),
			        add: function(e,data) {
			        	data.submit();
			        },
			        complete: $.proxy(this.templateImportComplete,this)
			        });
			},
			
			templateImportComplete : function() {
				this.reload();
			},
			
			addTemplateClicked : function(event) {
				var templateName = prompt("Template Name");
				if (!templateName) return;
				Link.addTemplate({projectId:this.project.id, templateName:templateName}).post($.proxy(this.templateAdded,this));
			},
			
			templateAdded : function() {
				this.reload();
			},
			
			templateClicked : function(event) {
				var $el = $(event.target);
				this.templateSelected($el);
				event.preventDefault();
			},
			
			templateSelected : function($el) {
				$(this.el).find("li").removeClass("active");
				$el.closest("li").addClass("active");
				
				var template = this.model.templates[$el.data("index")];
				this.selected = template;
				
				$(this).trigger("TemplateSelected",[template.id,template]);
			},
			
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
