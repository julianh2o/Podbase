define(
	['view/RenderedView', 'view/TabModule', 'view/ProjectSelect', 'view/TemplateManager', 'view/Permissions', 'view/ProjectSettings', 'view/Header', 'view/Footer', 'data/Link', 'util/Util', 'view/ImageBrowser', 'text!tmpl/Main.html'],
	function (RenderedView, TabModule, ProjectSelect, TemplateManager, Permissions, ProjectSettings, Header, Footer, Link, Util, ImageBrowser, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options){
				this.projectId = this.options.projectId;
				
				window.debug = true;
				this.render();
				
				this.$projectTabs = $(".content",this.el);
				this.selectedTab = "browser";
				
				Link.loadAll([
				                            ["getProject",{projectId:this.projectId}],
				                            ["getAccess",{modelId:this.projectId}]
				                            ],$.proxy(this.refresh,this),true);
				
				$("html").on("PathChanged ProjectSelected TemplateSelected TemplateChanged",Util.debugEvent);
			},
			
			refresh : function(projectLink,accessLink) {
				var project = projectLink.getData();
				
				this.header = Util.createView( $(".header",this.el), Header, {project: project});
				this.footer = Util.createView( $(".footer",this.el), Footer);
				
				var access = Util.permissionsAsMap(accessLink.getData());
				
				this.imageBrowser = new ImageBrowser({project:project,access:access});
				this.templateManager = new TemplateManager({project:project,access:access});
				this.projectPermissions = new Permissions({model:project,access:access,type:"project"});
				var tabs = [];
				
				tabs.push({
					id: "browser",
					label: "Image Browser",
					content: this.imageBrowser
				});
				
				if (access["VIEW_TEMPLATES"]) {
					tabs.push({
						id: "templates",
						label: "Template Manager",
						content: this.templateManager
					});
				}
				
				if (access["MANAGE_PERMISSIONS"]) {
					tabs.push({
						id: "permissions",
						label: "Project Permissions",
						content: this.projectPermissions
					});
				}
				
//				this.projectSettings = new ProjectSettings({project:project,access:access});
//				if (_.include(access,"PROJECT_MANAGE_DIRECTORIES")) {
//					tabs.push({
//						id: "settings",
//						label: "Project Settings",
//						content: this.projectSettings
//					});
//				}
				
				this.tabs = new TabModule({tabs: tabs,selectedTab: this.selectedTab });
				$(this.tabs).on("TabSelected",$.proxy(this.tabSelected,this));
				this.$projectTabs.empty().append(this.tabs.el);
			},
			
			tabSelected : function(e,id) {
				this.selectedTab = id;
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)