define(
		['view/RenderedView', 'util/Util', 'data/Link', 'view/ProjectSelect', 'view/ImageBrowserPaperMode', 'text!tmpl/PaperHome.html'],
		function (RenderedView, Util, Link, ProjectSelect, ImageBrowserPaperMode, tmpl) {

			var This = RenderedView.extend({
				template: _.template( tmpl ),

				initialize: function() {
					this.render();
					
					this.projectSelect = new ProjectSelect();
					this.$projectSelect = $(".project-select",this.el);
					this.$projectSelect.append(this.projectSelect.el);
					
					$("html").on("ProjectSelected",$.proxy(this.projectSelected,this));
				},
				
			
				projectSelected : function(e,projectId,project) {
					Link.getInstance().loadAll([
				                            ["paper",{paperId:paperId}],
				                            ["project",{projectId:projectId}],
				                            ["projectAccess",{projectId:projectId}]
				                            ],$.proxy(this.refresh,this));
				},
				
				refresh : function(paperLink, projectLink,accessLink) {
					var paper = paperLink.getData();
					var project = projectLink.getData();
					var access = accessLink.getData();
					
					this.imageBrowser = new ImageBrowserPaperMode({project:project,access:access,paperId:paper.id});
					$(".image-browser",this.el).empty().append(this.imageBrowser.el);
				}
			});

			$.extend(This,{
			});

			return This;
		}
)
