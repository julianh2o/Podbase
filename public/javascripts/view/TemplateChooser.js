define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'text!tmpl/TemplateChooser.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.templates = null;
				this.templateCache = new Cache();
				
				this.render();
				
				this.$inherited = $(".inherited",this.el);
				this.$inheritedBox = $(".inherited-box",this.el).hide();
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
				this.canChooseTemplate = Util.permits(this.access,"SET_TEMPLATE");
				
				this.refresh();
				
				this.updateTemplateSelection(this.selectedTemplate,this.inheritedFrom);
			},
			
			handleTemplateChange : function(event) {
				var templateId = event.currentTarget.value;
				var templateAssignment = this.templateCache.get(this.path);
				
				if (templateId) {
					Link.setFolderTemplate({projectId:this.project.id,templateId:templateId,path:this.path}).post().done($.proxy(this.handleTemplateForPath,this));
				} else {
					Link.clearFolderTemplate({projectId:this.project.id,path:this.path}).post().done($.proxy(this.handleTemplateForPath,this));
				}
				
				$(this).trigger("TemplateChanged",[templateId]);
			},
			
			setPath : function(path) {
				Util.assertPath(path);
				
				this.path = path;
				if (this.templateCache.has(path)) {
					this.setSelectedTemplate(this.templateCache.get(path));
				} else {
					this.templateCache.put(path,null);
					Link.getTemplateForPath({projectId:this.project.id,path:path}).post().done($.proxy(this.handleTemplateForPath,this));
				}
			},
			
			handleTemplateForPath : function(templateAssignment) {
				if (templateAssignment != null) {
					this.templateCache.put(templateAssignment.path,templateAssignment.template);
					var inheritedFrom = this.path != templateAssignment.path ? templateAssignment.path : null;
					this.setSelectedTemplate(templateAssignment.template,inheritedFrom);
				} else {
					this.setSelectedTemplate(null,null);
				}
			},
			
			setSelectedTemplate : function(template,inheritedFrom) {
				this.inheritedFrom = inheritedFrom;
				this.selectedTemplate = template;
				if (this.$select || this.$span) this.updateTemplateSelection(template,inheritedFrom);
			},
			
			updateTemplateSelection : function(template,inheritedFrom) {
				if (this.canChooseTemplate) {
					this.$select.val(template == null?"":template.id);
					if (inheritedFrom) {
						var dirName = Util.getFileName(inheritedFrom);
						this.$inheritedBox.show();
						this.$inherited.text(dirName);
					} else {
						this.$inheritedBox.hide();
					}
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