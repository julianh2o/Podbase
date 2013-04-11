define(
	['view/RenderedView', 'util/HashHandler', 'data/Link', 'util/Util', 'util/Cache', 'view/TemplateChooser', 'text!tmpl/FileBrowser.html'],
	function (RenderedView, HashHandler, Link, Util, Cache, TemplateChooser, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.project = options.project;
				this.root = options.root;
				this.showVisibility = options.showVisibility;
				this.render();
				
				this.$browser = $(".file-select",this.el);
				this.$path = $(".path",this.el);
				
				this.$browser.on("change",$.proxy(this.onBrowserChange,this));
				this.$browser.on("keydown",$.proxy(this.onBrowserKeydown,this));
				//this.$browser.on("click",function(e) {e.preventDefault()});
				this.$browser.on("dblclick",$.proxy(this.onBrowserTrigger,this));
				
				this.path = options.path || this.root;
				this.directoryCache = new Cache();
				
				$("html").on("FileSystemUpdated",$.proxy(this.reload,this));
				
				this.selectedFiles = [];
			},
			
			reload : function() {
				this.directoryCache.purge();
				this.loadPath(this.getSelectedPath());
			},
			
			loadDirectory : function(directoryPath, hashUpdate) {
				this.loadPath(directoryPath.chopEnd("/") + "/",hashUpdate);
			},
			
			loadRoot : function(hashUpdate) {
				this.loadPath(this.root,hashUpdate);
			},
			
			loadPath : function(path, hashUpdate) {
				Util.assertPath(path);
				
				if (!path.startsWith(this.root)) {
					throw "invalid path, doesnt start with root "+path+" "+this.root;
				}
				
				var selectedFileNames = [];
				if (Util.isDirectoryPath(path)) {
					this.path = path.chopEnd("/");
					selectedFileNames = [];
				} else {
					this.path = Util.getParentDirectory(path);
					selectedFileNames = [Util.getFileName(path)];
				}
				
				this.$path.empty().append(this.renderPath());
				$(this).trigger("PathChanged",[this.path]);
				
				if (!hashUpdate) {
					HashHandler.getInstance().setHash(path);
				}
				
				if (this.directoryCache.has(this.path)) {
					this.loadFiles(this.path, selectedFileNames,this.directoryCache.get(this.path));
				} else {
					var link = null;
					if (this.project) {
						link = Link.fetchProjectPath({projectId:this.project.id,path:this.path});
					} else {
						link = Link.fetchPath(this.path);
					}
					link.invalidate().loadOnce($.proxy(function(link) {
						files = link.getData();
						this.directoryCache.put(this.path, files);
						this.loadFiles(this.path, selectedFileNames, files);
					},this));
				}
			},

			//TODO make me a template
			loadFiles : function(path, selectedFileNames, files) {
				if (files == null) {
					return;
				}
				
				var that = this;
				var selection = false;

				function renderOption(file) {
					var option = new Option();

					option.text = (file.visible && that.showVisibility?"[V] ":"") + file.display + (file.isDir ? "/" : "");

					option.value = file.path;
					option.title = file.path;
					if (file && selectedFileNames && _.contains(selectedFileNames,file.display)) {
						option.selected = true;
						selection = true;
					}
					$(option).data("file", file)
					that.$browser.append(option);
				}

				this.$browser.empty();
				
				if (path + "/" != this.root) {
					renderOption({
						display: "..",
						isDir: true,
						isDotDot: true,
						path: Util.getParentDirectory(path)
					});
				}
				
				$.each(files, function() {
					renderOption(this);
				});
				
				if (!selection) {
					this.$browser.get(0).selectedIndex = 0;
					var file = this.$browser.children().eq(0).data("file");
				}
				
				this.handleSelectionUpdate();
				
				this.$browser.scrollTop(0);
			},
			
			updateSelectionFromBrowser : function() {
				var selectedElements = this.$browser.children(":selected");
				var files = [];
				selectedElements.each(function() {
					files.push($(this).data("file"))
				});
				this.selectedFiles = files;
			},

			getSelectedPath : function() {
				if (!this.selectedFiles || !this.selectedFiles.length) return this.path;
				if (this.selectedFiles.length > 1) return Util.appendPaths(this.path,"/");
				
				return this.selectedFiles[0].path;
			},
			
			onBrowserKeydown : function(e) {
				if (e.keyCode == 39 || e.keyCode == 13) {
					this.onBrowserTrigger();
				}
				
				if (e.keyCode == 37) {
					//left arrow
					if (!this.isRoot()) {
						this.loadDirectory(Util.getParentDirectory(this.path));
					}
				}
			},
			
			isRoot : function() {
				return this.path + "/" == this.root;
			},
			
			onBrowserTrigger : function() {
				if (this.selectedFiles.length != 1) return;
				
				var file = this.selectedFiles[0];
				if (file.isDir) {
					this.loadDirectory(file.path);
				}
			},
			
			selectionsEquivalent : function(a,b) {
				if (a.length != b.length) return false;
				for(var i=0; i<a.length; i++) {
					if (a[i].path != b[i].path) return false;
				}
				return true;
			},
			
			onBrowserChange : function(e) {
				this.handleSelectionUpdate();
			},
			
			handleSelectionUpdate : function() {
				var previouslySelectedFiles = this.selectedFiles;
				this.updateSelectionFromBrowser();
				
				if (this.selectionsEquivalent(previouslySelectedFiles,this.selectedFiles)) return;
				
				if (this.selectedFiles.length == 1) {
					var file = this.selectedFiles[0];
					
					$(this).trigger("PathSelected",[this.getSelectedPath(), file]);
					HashHandler.getInstance().replaceHash(this.getSelectedPath());
				} else if (this.selectedFiles.length > 1){
					HashHandler.getInstance().replaceHash(this.getSelectedPath());
					$(this).trigger("MultipleSelected",[this.getSelectedPath(), this.selectedFiles]);
				} else {
					HashHandler.getInstance().replaceHash(this.getSelectedPath());
					$(this).trigger("PathDeselected");
				}
			},
			
			optionTriggered : function(event) {
				var option = event.target;
				var file = $(option).data("file");
			},
			
			renderPath : function() {
				var $container = $("<ul class='pathSegments' />");
				var pieces = this.path.substring(1).split("/");
				var pathToCurrent = "";
				var self = this;
				_.each(pieces,function(tok,index) {
					pathToCurrent += "/"+tok;
					var seg = $("<li />");
					
					if (pathToCurrent == self.path) {
						seg.text(tok);
					} else {
						var a = $("<a />");
						a.text(tok);
						a.attr("href","#");
						
						a.click(function(e) {
							e.preventDefault();
							self.loadDirectory(myPathToCurrent,false);
						});
					}
					
					var myPathToCurrent = pathToCurrent+"";
					seg.append(a);
					
					$container.append(seg);
				});
				return $container;
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
