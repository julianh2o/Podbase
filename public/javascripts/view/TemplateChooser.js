define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'text!tmpl/TemplateChooser.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.templates = null;
				this.templateCache = new Cache();
				
				this.render();
				
				this.$select = $("select",this.el);
				this.$select.change($.proxy(this.handleTemplateChange,this));
				
				$("html").on("PathChanged",$.proxy(this.pathChanged,this));
				
				this.setProject(this.options.projectId);
			},
			
			setProject : function(projectId) {
				this.projectId = projectId;
				
				Link.getInstance().template.get({projectId:this.projectId}).asap($.proxy(this.templatesLoaded,this));
			},
			
			refresh : function() {
				this.$select.empty();
				
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
		
			templatesLoaded : function(link) {
				this.templates = link.getData();
				
				this.refresh();
			},
			
			handleTemplateChange : function(event) {
				var templateId = event.currentTarget.value;
				var templateAssignment = this.templateCache.get(this.path);
				
				$.post("@{TemplateController.setFolderTemplate()}", {projectId:this.projectId,templateId:templateId,path:this.path},$.proxy(this.handleTemplateForPath,this),'json');
				
				$("html").trigger("TemplateChanged",[templateId]);
			},
			
			pathChanged : function(path) {
				this.path = path;
				if (this.templateCache.has(path)) {
					this.setSelectedTemplate(this.templateCache.get(path));
				} else {
					this.templateCache.put(path,null);
					$.post("@{TemplateController.getTemplateForPath()}", {projectId:this.projectId,path:path},$.proxy(this.handleTemplateForPath,this), 'json');
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
				if (template == null) {
					this.$select.val("");
				} else {
					this.$select.val(template.id);
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)