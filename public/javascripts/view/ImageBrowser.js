define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'data/Link', 'util/Cache', 'view/TemplateChooser', 'view/ImageBrowserActionBar', 'view/ImageDetails', 'view/FileBrowser', 'view/ImagePreview', 'view/SearchPanel', 'text!tmpl/ImageBrowser.html'],
	function (RenderedView, HashHandler, Util, Link, Cache, TemplateChooser, ImageBrowserActionBar, ImageDetails, FileBrowser, ImagePreview, SearchPanel, tmpl) {
		
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
				
				this.searchPanel = Util.createView( $(".image-search",this.el), SearchPanel, {project:this.project});
				
				this.$browser = $(".filebrowser",this.el);
				this.$preview = $(".preview",this.el);
				this.$metadata = $(".metadata",this.el);
				
				this.templateChooser = new TemplateChooser({project:this.project});
				$(".templatechooser",this.el).append(this.templateChooser.el);
				
				this.path = this.options.path;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				this.fileBrowser = new FileBrowser({root:"/"+this.project.name,project:this.project, showVisibility: _.contains(this.access,"EDITOR")});
				this.$browser.append(this.fileBrowser.el);
				
				$(".path",this.actionBar.el).replaceWith($(".path",this.fileBrowser.el));
				
				this.templateChooser.setPath("/");
				
				this.imagePreview = new ImagePreview({browser:this});
				this.$preview.append(this.imagePreview.el);
				
				this.imageDetails = new ImageDetails({project:this.project,access:this.access,dataMode:this.dataMode});
				this.$metadata.append(this.imageDetails.el);
				
				$(this.fileBrowser).on("PathChanged",$.proxy(this.onPathChanged,this));
				$(this.fileBrowser).on("PathSelected",$.proxy(this.onPathSelected,this));
				$(this.fileBrowser).on("MultipleSelected",$.proxy(this.onMultipleSelected,this));
				$(this.fileBrowser).on("PathDeselected",$.proxy(this.onPathDeselected,this));
				
				$(this.fileBrowser).on("PathChanged PathSelected PathDeselected", Util.debugEvent);
				
				$(HashHandler.getInstance()).bind('HashUpdate', $.proxy(this.loadHashPath,this));
				
				$(this.templateChooser).on("TemplateChanged",$.proxy(this.imageDetails.reload,this.imageDetails))
				
				Link.getProject({projectId:this.project.id}).dataReady($.proxy(this.projectReloaded,this));
				
				$(".import-data",this.el).click($.proxy(this.importDataClicked,this));
				
				this.loadHashPath();
			},
			
			importDataClicked : function() {
				var path = this.fileBrowser.getSelectedPath();
				Link.findImportables(path).loadOnce($.proxy(this.importDataLoaded,this));
			},
			
			importDataLoaded : function(loader) {
				var data = loader.getData();
				if (!data.length) {
					alert("There is no unimported data to import.");
					return;
				}
				var paths = "";
				_.each(data,function(image) {
					paths += image.path+"\n";
				});
				var yes = confirm("Really import data?\n\n"+paths);
				if (yes) {
					Link.importDirectory(this.project.id,this.fileBrowser.getSelectedPath()).post($.proxy(this.dataImportSuccess,this));
				}
			},
			
			dataImportSuccess : function() {
				alert("Data imported!");
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
					return;
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
			
			onMultipleSelected : function(e,path,files) {
				this.imagePreview.loadPreview(files);
				this.imageDetails.setFile(null);
			},
			
			onPathDeselected : function(e) {
				this.imagePreview.loadPreview(null);
				this.imageDetails.setFile(null);
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
