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
				this.$browser.on("dblclick",$.proxy(this.onBrowserTrigger,this));
				
				this.path = options.path || this.root;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				this.loadPath(this.path,null,true);
			},
			
			reload : function() {
				this.directoryCache.purge();
				this.loadPath(this.path,this.selectedFile);
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
							self.loadPath(myPathToCurrent,null,false);
						});
					}
					
					var myPathToCurrent = pathToCurrent+"";
					seg.append(a);
					
					$container.append(seg);
				});
				return $container;
			},
			
			loadPath : function(path, selectedFile,hashUpdate) {
				Util.assertPath(path);
				this.selectedFile = null;
				this.path = path;
				
				this.$path.empty().append(this.renderPath());
				$(this).trigger("PathChanged",[this.path]);
				
				if (!hashUpdate) {
					var hashpath = this.getSelectedPath();
					if (hashpath != "/") hashpath += "/";
					HashHandler.getInstance().setHash(hashpath);
				}

				if (this.directoryCache.has(path)) {
					this.loadFiles(path, selectedFile, this.directoryCache.get(path));
				} else {
					var self = this;
					var link = null;
					if (this.project) {
						link = Link.fetchProjectPath({projectId:this.project.id,path:path});
					} else {
						link = Link.fetchPath(path);
					}
					link.invalidate().loadOnce(function(link) {
						files = link.getData();
						self.directoryCache.put(path, files);
						self.loadFiles(path, selectedFile, files);
					});
				}
			},

			loadFiles : function(path, selectedName, files) {
				if (files == null) {
					return;
				}
				
				var that = this;
				var selectedOption = null;
				var selectedFileObject = null;

				function renderOption(file) {
					var option = new Option();

					option.text = (file.visible && that.showVisibility?"[V] ":"") + file.display + (file.isDir ? "/" : "");

					option.value = file.path;
					option.title = file.path;
					if (file && selectedName && file.display == selectedName) {
						option.selected = true;
						selectedFileObject = file;
						selectedOption = option;
					}
					$(option).data("file", file)
					that.$browser.append(option);
				}

				this.$browser.empty();
				
				if (path != this.root) {
					renderOption({
						display: "..",
						isDir: true,
						isMagic: true,
						path: Util.getParentDirectory(path)
					});
				}
				
				$.each(files, function() {
					renderOption(this);
				});
				
				if (selectedFileObject) {
					this.fileSelected(selectedFileObject);
				} else {
					this.$browser.get(0).selectedIndex = 0;
				}
				
				this.$browser.scrollTop(0);
			},

			getSelectedPath : function() {
				if (!this.selectedFile) return this.path;
				if (this.selectedFile.length) return this.path + "/";
				
				return this.selectedFile.path;
			},
			
			onBrowserKeydown : function(e) {
				if (e.keyCode == 39 || e.keyCode == 13) {
					this.onBrowserTrigger();
				}
				
				if (e.keyCode == 37) {
					//left arrow
					this.loadPath(Util.getParentDirectory(this.path),null);
				}
			},
			
			onBrowserTrigger : function() {
				if (this.selectedFiles.length != 1) return;
				
				var file = this.selectedFiles[0];
				if (file.isDir) {
					this.loadPath(file.path, null);
				}
			},
			
			onBrowserChange : function(e) {
				var selectedElements = this.$browser.children(":selected");
				var files = [];
				selectedElements.each(function() {
					files.push($(this).data("file"))
				});
				this.selectedFiles = files;
				
				if (selectedElements.length == 1) {
					var file = files[0];
					if (this.selectedFile != file) this.fileSelected(file);
				} else if (selectedElements.length > 1){
					this.multipleSelected(files);
				} else {
					this.fileDeselected();
				}
			},
			
			multipleSelected : function(files) {
				this.selectedFile = files;
				HashHandler.getInstance().replaceHash(this.getSelectedPath());
				
				$(this).trigger("MultipleSelected",[this.getSelectedPath(), files]);
			},
			
			fileDeselected : function() {
				if (this.selectedFile == null) return;
				this.selectedFile = null;
				$(this).trigger("PathDeselected");
				HashHandler.getInstance().replaceHash(this.getSelectedPath());
				return;
			},

			fileSelected : function(file) {
				if (file.isMagic) {
					this.fileDeselected();
					return;
				}
				
				this.selectedFile = file;
				$(this).trigger("PathSelected",[this.getSelectedPath(), file]);
				HashHandler.getInstance().replaceHash(this.getSelectedPath());
			},
			
			optionTriggered : function(event) {
				var option = event.target;
				var file = $(option).data("file");
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
