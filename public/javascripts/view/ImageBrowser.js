define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'data/Link', 'util/Cache', 'view/TemplateChooser', 'view/ImageBrowserActionBar', 'view/ImageDetails', 'view/FileBrowser', 'view/ImagePreview', 'text!tmpl/ImageBrowser.html'],
	function (RenderedView, HashHandler, Util, Link, Cache, TemplateChooser, ImageBrowserActionBar, ImageDetails, FileBrowser, ImagePreview, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.project = this.options.project;
				this.access = this.options.access;
				this.dataMode = this.project.dataMode;
				
				this.render();
				
				this.actionBar = Util.createView( $(".actions",this.el), ImageBrowserActionBar, {dataMode: this.dataMode, access:this.access});
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
				
				this.fileBrowser = new FileBrowser({project:this.project, showVisibility: _.contains(this.access,"EDITOR")});
				this.$browser.append(this.fileBrowser.el);
				
				$(".path",this.fileBrowser.el).remove().appendTo($(".path",this.actionBar.el));
				
				this.templateChooser.setPath("/");
				
				this.imagePreview = new ImagePreview({browser:this});
				this.$preview.append(this.imagePreview.el);
				
				this.imageDetails = new ImageDetails({project:this.project,access:this.access,dataMode:this.dataMode});
				this.$metadata.append(this.imageDetails.el);
				
				$(this.fileBrowser).on("PathChanged",$.proxy(this.onPathChanged,this));
				$(this.fileBrowser).on("PathSelected",$.proxy(this.onPathSelected,this));
				
				$(this.fileBrowser).on("PathChanged PathSelected PathDeselected", Util.debugEvent);
				
				$(HashHandler.getInstance()).bind('HashUpdate', $.proxy(this.loadHashPath,this));
				
				$(this.templateChooser).on("TemplateChanged",$.proxy(this.imageDetails.reload,this.imageDetails))
				
				Link.getProject({projectId:this.project.id}).dataReady($.proxy(this.projectReloaded,this));
				
				this.loadHashPath();
			},
			
			projectReloaded : function() {
				this.fileBrowser.reload();
			},
			
			dataModeChanged : function(e,mode) {
				this.dataMode = mode;
				this.imageDetails.setDataMode(mode);
				Link.setDataMode({projectId:this.project.id, dataMode: mode}).post();
			},
			
			loadHashPath : function() {
				var pageParameters = Util.parseLocationHash();
				var path = pageParameters[" "];
				var selectedFile = Util.getFileName(path);
				
				path = path.substring(0, path.lastIndexOf("/"));
				
				if (path == "" || path == undefined) {
					path = "/";
				}
				
				Util.assertPath(path);
				this.fileBrowser.loadPath(path, selectedFile,true);
			},
			
			onPathChanged : function(e,path) {
				this.templateChooser.setPath(path);
			},
			
			onPathSelected : function(e,path,file) {
				Util.assertPath(path);
				
				this.imagePreview.loadPreview(file);
				this.imageDetails.setFile(file);
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
