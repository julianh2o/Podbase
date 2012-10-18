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
				this.model = {templates:project ? project.templates : null};
				this.refresh();
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
				this.render();
				
				$(".template",this.el).click($.proxy(this.templateClicked,this));
				$(".add",this.el).click($.proxy(this.addTemplateClicked,this));
			},
			
			addTemplateClicked : function(event) {
				var templateName = prompt("Template Name");
				if (!templateNme) return;
				Link.getInstance().addTemplate({projectId:this.project.id, templateName:templateName},$.proxy(this.templateAdded,this));
			},
			
			templateAdded : function() {
				Link.getInstance().getProjects.pull();
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
