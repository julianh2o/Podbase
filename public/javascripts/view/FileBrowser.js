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

					option.text = (file.visible && that.showVisibility?"(visible) ":"") + file.display + (file.isDir ? "/" : "");

					option.value = file.path;
					option.title = file.path;
					if (file && selectedName && file.display == selectedName) {
						option.selected = true;
						selectedFileObject = file;
						selectedOption = option;
					}
					$(option).data("file", file)
					$(option).on("click",$.proxy(that.optionSelected,that));
					$(option).bind('dblclick', {browser:that}, $.proxy(that.optionTriggered,that));
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
				
				if (selectedFileObject) this.fileSelected(selectedFileObject);
				
				this.$browser.scrollTop(0);
			},

			getSelectedPath : function() {
				if (!this.selectedFile) return this.path;
				
				return this.selectedFile.path;
			},
			
			optionSelected : function(event) {
				var $option = $(event.target);
				var file = $option.data("file");
				var selected = $option.is(":selected");
				
				if (selected) {
					if (this.selectedFile != file) this.fileSelected(file);
				} else {
					this.fileDeselected();
				}
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
				var that = event.data.browser;
				var option = event.target;
				var file = $(option).data("file");
				if (file.isDir) {
					that.loadPath(file.path, null);
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
