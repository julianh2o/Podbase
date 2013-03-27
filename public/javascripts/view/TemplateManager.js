define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'view/TemplateChooser', 'view/TemplateList', 'view/TemplateEditor', 'text!tmpl/TemplateManager.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, TemplateChooser, TemplateList, TemplateEditor, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.project = this.options.project;
				
				this.render();
				
				this.$templates = $(".templates",this.el);
				this.$editor = $(".editor",this.el);
				
				this.templateList = new TemplateList({project:this.project});
				this.$templates.append(this.templateList.el);
				
				// SELECT FIRST TEMPLATE
				if (this.templateList.getSelectedTemplate() == null) {
					this.templateList.selectIndex(0);
				}
				
				this.templateEditor = new TemplateEditor({project:this.project});
				this.$editor.append(this.templateEditor.el);
				
				$(this.templateList)
					.on("TemplateSelected",$.proxy(this.templateSelected,this))
					.on("TemplateDeselected",$.proxy(this.templateDeselected,this))
					
					.on("TemplateDeselected",Util.debugEvent)
					.on("TemplateSelected",Util.debugEvent);
			},
			
			templateDeselected : function(e) {
				this.templateEditor.setTemplate(null);
			},
			
			templateSelected : function(e,templateId,template) {
				this.templateEditor.setTemplate(template);
			},
			
			returnStatus : function(response) {
				
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
