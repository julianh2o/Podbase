define(
	['view/RenderedView', 'view/TabModule', 'view/ProjectSelect', 'view/TemplateManager', 'view/ProjectPermissions', 'view/ProjectSettings', 'view/UserPanel', 'data/Link', 'util/Util', 'view/ImageBrowser', 'text!tmpl/Main.html'],
	function (RenderedView, TabModule, ProjectSelect, TemplateManager, ProjectPermissions, ProjectSettings, UserPanel, Link, Util, ImageBrowser, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options){
				this.projectId = this.options.projectId;
				
				window.debug = true;
				this.render();
				
				//this.projectSelect = new ProjectSelect();
				//this.$projectSelect = $(".project-select",this.el);
				//this.$projectSelect.append(this.projectSelect.el);
				
				this.userPanel = new UserPanel();
				this.$userPanel = $(".user-panel",this.el);
				this.$userPanel.append(this.userPanel.el);
				
				this.$projectTabs = $(".project-tabs",this.el);
				
				Link.getInstance().loadAll([
				                            ["project",{projectId:this.projectId}],
				                            ["projectAccess",{projectId:this.projectId}]
				                            ],$.proxy(this.refresh,this));
				
				$("html").on("PathChanged ProjectSelected TemplateSelected TemplateChanged",Util.debugEvent);
			},
			
			refresh : function(projectLink,accessLink) {
				var project = projectLink.getData();
				var access = accessLink.getData();
				
				this.imageBrowser = new ImageBrowser({project:project,access:access});
				this.templateManager = new TemplateManager({project:project,access:access});
				this.projectPermissions = new ProjectPermissions({project:project,access:access});
				this.projectSettings = new ProjectSettings({project:project,access:access});
				var tabs = [];
				
				tabs.push({
					id: "browser",
					label: "Image Browser",
					content: this.imageBrowser
				});
				
				if (_.include(access,"editTemplates")) {
					tabs.push({
						id: "templates",
						label: "Template Manager",
						content: this.templateManager
					});
				}
				
				if (_.include(access,"managePermissions")) {
					tabs.push({
						id: "permissions",
						label: "Project Permissions",
						content: this.projectPermissions
					});
				}
				
				if (_.include(access,"manageProject")) {
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