define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'view/TemplateChooser', 'view/ImageDetails', 'view/FileBrowser', 'view/ImagePreview', 'text!tmpl/ImageBrowser.html'],
	function (RenderedView, HashHandler, Util, Cache, TemplateChooser, ImageDetails, FileBrowser, ImagePreview, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.$browser = $(".browser",this.el);
				this.$preview = $(".preview",this.el);
				this.$metadata = $(".metadata",this.el);
				
				this.templateChooser = new TemplateChooser();
				$(".templatechooser",this.el).append(this.templateChooser.el);
				
				$("html").on("ProjectSelected",$.proxy(this.onProjectSelected,this));
				
				this.path = this.options.path;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				//Do something with this code.. lol
				
				$(window).bind('hashEdited', function(str) {
					function loadCurrentPath() {
						var pageParameters = Util.parseLocationHash();
						var path = pageParameters[" "];
						if (path == "" || path == undefined) {
							path = "/";
						}
						var selectedFile = Util.getFileName(path);
						path = path.substring(0, path.lastIndexOf("/"));
						browser.loadPath(path, selectedFile);
					}
					loadCurrentPath();
				});
				
				this.fileBrowser = new FileBrowser();
				this.$browser.append(this.fileBrowser.el);
				
				this.imagePreview = new ImagePreview();
				this.$preview.append(this.imagePreview.el);
				
				this.imageDetails = new ImageDetails();
				this.$metadata.append(this.imageDetails.el);
				
				$(this.fileBrowser).on("PathChanged PathSelected PathDeselected", Util.debugEvent);
				$(this.fileBrowser).on("PathSelected",$.proxy(this.onPathSelected,this));
			},
			
			onProjectSelected : function(e,projectId) {
				this.templateChooser.setProject(projectId);
				this.fileBrowser.setProject(projectId);
				this.projectId = projectId;
			},
			
			onPathSelected : function(e,path,file) {
				if (!file.isDir) {
					this.imagePreview.loadPreview(file);
					this.imageDetails.loadDetails(file);
				}
			},
			
			clear : function() {
				this.$metadata.empty();
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
