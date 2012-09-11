define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'view/TemplateChooser', 'view/ImageDetails', 'view/FileBrowser', 'view/ImagePreview', 'text!tmpl/ImageBrowser.html'],
	function (RenderedView, HashHandler, Util, Cache, TemplateChooser, ImageDetails, FileBrowser, ImagePreview, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.project = this.options.project;
				this.access = this.options.access;
				
				this.render();
				
				this.$browser = $(".filebrowser",this.el);
				this.$preview = $(".preview",this.el);
				this.$metadata = $(".metadata",this.el);
				
				this.templateChooser = new TemplateChooser({project:this.project});
				$(".templatechooser",this.el).append(this.templateChooser.el);
				
				this.path = this.options.path;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				$(HashHandler.getInstance()).bind('HashUpdate', $.proxy(this.loadHashPath,this));
				
				this.fileBrowser = new FileBrowser({project:this.project});
				this.$browser.append(this.fileBrowser.el);
				
				this.templateChooser.setPath("/");
				
				this.imagePreview = new ImagePreview();
				this.$preview.append(this.imagePreview.el);
				
				this.imageDetails = new ImageDetails({access:this.access});
				this.$metadata.append(this.imageDetails.el);
				
				$(this.fileBrowser).on("PathChanged",$.proxy(this.onPathChanged,this));
				$(this.fileBrowser).on("PathSelected",$.proxy(this.onPathSelected,this));
				
				$(this.fileBrowser).on("PathChanged PathSelected PathDeselected", Util.debugEvent);
				
				this.loadHashPath();
			},
			
			loadHashPath : function(hash) {
				var pageParameters = Util.parseLocationHash();
				var path = pageParameters[" "];
				if (path == "" || path == undefined) {
					path = "/";
				}
				var selectedFile = Util.getFileName(path);
				path = path.substring(0, path.lastIndexOf("/"));
				this.fileBrowser.loadPath(path, selectedFile);
			},
			
			onPathChanged : function(e,path) {
				this.templateChooser.setPath(path);
			},
			
			onPathSelected : function(e,path,file) {
				if (!file.isDir) {
					this.imagePreview.loadPreview(file);
					this.imageDetails.loadDetails(file);
				}
				
				HashHandler.getInstance().setHash(path);
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
