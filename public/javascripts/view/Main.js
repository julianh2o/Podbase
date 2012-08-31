define(
	['view/RenderedView', 'view/TabModule', 'view/ProjectSelect', 'view/TemplateManager', 'view/ProjectPermissions', 'view/ProjectSettings', 'data/Link', 'util/Util', 'view/ImageBrowser', 'text!tmpl/Main.html'],
	function (RenderedView, TabModule, ProjectSelect, TemplateManager, ProjectPermissions, ProjectSettings, Link, Util, ImageBrowser, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(){
				window.debug = true;
				this.render();
				
				this.projectSelect = new ProjectSelect();
				
				this.$projectSelect = $(".project-select",this.el);
				this.$projectTabs = $(".project-tabs",this.el);
				
				this.$projectSelect.append(this.projectSelect.el);
				
				$("html").on("ProjectSelected",$.proxy(this.projectSelected,this));
				
				$("html").on("PathChanged ProjectSelected TemplateSelected TemplateChanged",Util.debugEvent);
			},
			
			projectSelected : function(e,projectId,project) {
				Link.getInstance().currentPermissions.get({projectId:projectId}).asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var project = this.projectSelect.getSelectedProject();
				
				var permissions = _.pluck(Link.getInstance().currentPermissions.get({projectId:project.id}).getData(),'permission');
				
				this.imageBrowser = new ImageBrowser({project:project});
				this.templateManager = new TemplateManager({project:project});
				this.projectPermissions = new ProjectPermissions({project:project});
				this.projectSettings = new ProjectSettings({project:project});
				var tabs = [];
				
				tabs.push({
					id: "browser",
					label: "Image Browser",
					content: this.imageBrowser
				});
				
				if (_.include(permissions,"editTemplates")) {
					tabs.push({
						id: "templates",
						label: "Template Manager",
						content: this.templateManager
					});
				}
				
				if (_.include(permissions,"managePermissions")) {
					tabs.push({
						id: "permissions",
						label: "Project Permissions",
						content: this.projectPermissions
					});
				}
				
				if (_.include(permissions,"manageProject")) {
					tabs.push({
						id: "settings",
						label: "Project Settings",
						content: this.projectSettings
					});
				}
				
				this.tabs = new TabModule({tabs: tabs});
				this.$projectTabs.empty().append(this.tabs.el);
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)