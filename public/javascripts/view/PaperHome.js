define(
		['view/RenderedView', 'util/Util', 'data/Link', 'view/ProjectSelect', 'view/TabModule', 'view/Permissions', 'view/Header', 'view/ImageBrowserPaperMode', 'text!tmpl/Main.html'],
		function (RenderedView, Util, Link, ProjectSelect, TabModule, Permissions, Header, ImageBrowserPaperMode, tmpl) {

			var This = RenderedView.extend({
				template: _.template( tmpl ),

				initialize: function() {
					this.render();
					
					this.header = Util.createView( $(".header",this.el), Header, {projectSelect: true});
					
					this.$tabs = $(".content",this.el);
					
					$("html").on("ProjectSelected",$.proxy(this.projectSelected,this));
				},
				
			
				projectSelected : function(e,projectId,project) {
					Link.loadAll([
				                            ["getPaper",{paperId:paperId}],
				                            ["getProject",{projectId:projectId}],
				                            ["getAccess",{modelId:paperId}]
				                            ],$.proxy(this.refresh,this),true);
				},
				
				refresh : function(paperLink, projectLink,accessLink) {
					var paper = paperLink.getData();
					var project = projectLink.getData();
					var access = accessLink.getData();
					
					var tabs = [];
					
					this.imageBrowser = new ImageBrowserPaperMode({project:project,access:access,paperId:paper.id});
					this.permissions = new Permissions({model:paper,access:access,type:"paper"});
					
					tabs.push({
						id: "browser",
						label: "Image Browser",
						content: this.imageBrowser
					});
					
					if (Util.permits(access,"MANAGE_PERMISSIONS")) {
						tabs.push({
							id: "permissions",
							label: "Permissions Manager",
							content: this.permissions
						});
					}
					
					this.tabs = new TabModule({tabs: tabs,selectedTab: this.selectedTab });
					$(this.tabs).on("TabSelected",$.proxy(this.tabSelected,this));
					this.$tabs.empty().append(this.tabs.el);
				}
			});

			$.extend(This,{
			});

			return This;
		}
)