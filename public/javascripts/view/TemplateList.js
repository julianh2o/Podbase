define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'view/TemplateChooser', 'text!tmpl/TemplateList.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, TemplateChooser, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.setProject(this.options.project);
			},
			
			setProject : function(project) {
				this.project = project;
				if (this.project) {
					this.link = Link.getProject(this.project.id);
					this.refresh();
				}
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
			
			reload : function() {
				this.link.loadOnce($.proxy(this.dataReloaded,this));
				this.link.pull();
			},
			
			dataReloaded : function() {
				this.project = this.link.getData();
				this.refresh();
			},
			
			refresh : function() {
				this.model = {templates:this.project ? this.project.templates : null};
				this.render();
				
				$(".template",this.el).click($.proxy(this.templateClicked,this));
				$(".add",this.el).click($.proxy(this.addTemplateClicked,this));
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
