define(
		['view/RenderedView', 'util/Util', 'data/Link', 'view/ProjectSelect', 'view/Header', 'view/ImageBrowserPaperMode', 'text!tmpl/Main.html'],
		function (RenderedView, Util, Link, ProjectSelect, Header, ImageBrowserPaperMode, tmpl) {

			var This = RenderedView.extend({
				template: _.template( tmpl ),

				initialize: function() {
					this.render();
					
					this.header = Util.createView( $(".header",this.el), Header, {projectSelect: true});
					
					$("html").on("ProjectSelected",$.proxy(this.projectSelected,this));
				},
				
			
				projectSelected : function(e,projectId,project) {
					Link.getInstance().loadAll([
				                            ["getPaper",{paperId:paperId}],
				                            ["getProject",{projectId:projectId}],
				                            ["getUserProjectAccess",{projectId:projectId}]
				                            ],$.proxy(this.refresh,this),true);
				},
				
				refresh : function(paperLink, projectLink,accessLink) {
					var paper = paperLink.getData();
					var project = projectLink.getData();
					var access = accessLink.getData();
					
					this.imageBrowser = new ImageBrowserPaperMode({project:project,access:access,paperId:paper.id});
					$(".content",this.el).empty().append(this.imageBrowser.el);
				}
			});

			$.extend(This,{
			});

			return This;
		}
)