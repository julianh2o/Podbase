define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'text!tmpl/TemplateChooser.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.templates = null;
				this.templateCache = new Cache();
				
				this.render();
				
				this.$template = $(".template",this.el);
				
				this.setProject(this.options.project);
			},
			
			setProject : function(project) {
				this.project = project;
				
				Link.loadAll([
				                          ["getTemplates",{projectId:this.project.id}],
				                          ["getAccess",{modelId:this.project.id}]
				                          ],$.proxy(this.templatesLoaded,this),true);
			},
			
			refresh : function() {
				this.$template.empty();
				if (this.canChooseTemplate) {
					this.renderSelector();
				} else {
					this.$select = null;
					this.$span = $("<span class='chosen-template'/>");
					this.$template.append(this.$span);
				}
			},
			
			renderSelector : function() {
				this.$select = $("<select class='template-select'></select>");
				this.$template.append(this.$select);
				
				this.$select.change($.proxy(this.handleTemplateChange,this));
				
				var that = this;
				function addOption(text,value) {
					var option = $("<option />");
					option.html(text);
					option.attr("value",value);
					that.$select.append(option);
					return option;
				}
				
				addOption("-- None --", "");
				
				$.each(this.templates, function() {
					addOption(this.name, this.id);
				});
			},
		
			templatesLoaded : function() {
				this.templates = Link.getTemplates({projectId:this.project.id}).getData();
				this.access = Link.getAccess({modelId:this.project.id}).getData();
				this.canChooseTemplate = $.inArray("PROJECT_SET_TEMPLATE",this.access) >= 0;
				
				this.refresh();
				
				this.updateTemplateSelection(this.selectedTemplate)
			},
			
			handleTemplateChange : function(event) {
				var templateId = event.currentTarget.value;
				var templateAssignment = this.templateCache.get(this.path);
				
				Link.setFolderTemplate({projectId:this.project.id,templateId:templateId,path:this.path}).post($.proxy(this.handleTemplateForPath,this));
				
				$(this).trigger("TemplateChanged",[templateId]);
			},
			
			setPath : function(path) {
				this.path = path;
				if (this.templateCache.has(path)) {
					this.setSelectedTemplate(this.templateCache.get(path));
				} else {
					this.templateCache.put(path,null);
					Link.getTemplateForPath({projectId:this.project.id,path:path}).post($.proxy(this.handleTemplateForPath,this));
				}
			},
			
			handleTemplateForPath : function(templateAssignment) {
				if (templateAssignment != null) {
					this.templateCache.put(templateAssignment.path,templateAssignment.template);
					this.setSelectedTemplate(templateAssignment.template);
				} else {
					this.setSelectedTemplate(null);
				}
			},
			
			setSelectedTemplate : function(template) {
				this.selectedTemplate = template;
				if (this.$select || this.$span) this.updateTemplateSelection(template)
			},
			
			updateTemplateSelection : function(template) {
				if (this.canChooseTemplate) {
					this.$select.val(template == null?"":template.id);
				} else {
					this.$span.html(template == null? "[No Template]" : template.name);
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)