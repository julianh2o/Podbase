define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'view/TemplateChooser', 'view/ImageBrowserActionBar', 'view/ImageDetails', 'view/FileBrowser', 'view/ImagePreview', 'text!tmpl/ImageBrowser.html'],
	function (RenderedView, HashHandler, Util, Cache, TemplateChooser, ImageBrowserActionBar, ImageDetails, FileBrowser, ImagePreview, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.project = this.options.project;
				this.access = this.options.access;
				this.dataMode = this.project.dataMode;
				
				this.render();
				
				this.actionBar = Util.createView( $(".actions",this.el), ImageBrowserActionBar, {dataMode: this.dataMode});
				$(this.actionBar).on("DataModeChanged",Util.debugEvent);
				$(this.actionBar).on("DataModeChanged",$.proxy(this.dataModeChanged,this));
				
				this.$browser = $(".filebrowser",this.el);
				this.$preview = $(".preview",this.el);
				this.$metadata = $(".metadata",this.el);
				
				this.templateChooser = new TemplateChooser({project:this.project});
				$(".templatechooser",this.el).append(this.templateChooser.el);
				
				this.path = this.options.path;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				this.fileBrowser = new FileBrowser({project:this.project});
				this.$browser.append(this.fileBrowser.el);
				
				this.templateChooser.setPath("/");
				
				this.imagePreview = new ImagePreview();
				this.$preview.append(this.imagePreview.el);
				
				this.imageDetails = new ImageDetails({project:this.project,access:this.access,dataMode:this.dataMode});
				this.$metadata.append(this.imageDetails.el);
				
				$(this.fileBrowser).on("PathChanged",$.proxy(this.onPathChanged,this));
				$(this.fileBrowser).on("PathSelected",$.proxy(this.onPathSelected,this));
				
				$(this.fileBrowser).on("PathChanged PathSelected PathDeselected", Util.debugEvent);
				
				$(HashHandler.getInstance()).bind('HashUpdate', $.proxy(this.loadHashPath,this));
				
				$(this.templateChooser).on("TemplateChanged",$.proxy(this.imageDetails.reload,this.imageDetails))
				
				this.loadHashPath();
			},
			
			dataModeChanged : function(e,mode) {
				this.dataMode = mode;
				this.imageDetails.setDataMode(mode);
				Link.getInstance().setDataMode({projectId:this.project.id, dataMode: mode});
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
					this.imageDetails.setFile(file);
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
