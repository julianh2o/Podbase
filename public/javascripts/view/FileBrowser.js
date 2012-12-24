define(
	['view/RenderedView', 'util/HashHandler', 'data/Link', 'util/Util', 'util/Cache', 'view/TemplateChooser', 'text!tmpl/FileBrowser.html'],
	function (RenderedView, HashHandler, Link, Util, Cache, TemplateChooser, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.project = options.project;
				this.showVisibility = options.showVisibility;
				this.render();
				
				this.$browser = $(".file-select",this.el);
				this.$path = $(".path",this.el);
				
				this.path = options.path;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				this.loadPath(this.path);
			},
			
			reload : function() {
				this.directoryCache.purge();
				this.loadPath(this.path,this.selectedFile);
			},
			
			loadPath : function(path, selectedFile) {
				path = path || "/";
				path = path.chopEnd("/");
				if (path == "") {
					path = "/";
				} else {
					this.path = path + "/";
				}
				
				this.$path.html(this.path);
				$(this).trigger("PathChanged",[this.path]);

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

			loadFiles : function(path, selectedFile, files) {
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
					if (file && selectedFile && file.display == selectedFile.display) {
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
				
				if (path != "/") {
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
				
				return Util.appendPaths(this.path, this.selectedFile.display);
			},
			
			optionSelected : function(event) {
				var $option = $(event.target);
				var file = $option.data("file");
				
				this.fileSelected(file);
			},

			fileSelected : function(file) {
				if (file.isMagic) {
					if (this.selectedFile == null) return;
					
					this.selectedFile = null;
					$(this).trigger("PathDeselected");
					HashHandler.getInstance().setHash(this.getSelectedPath());
					return;
				}
				
				this.selectedFile = file;
				$(this).trigger("PathSelected",[this.getSelectedPath(), file]);
				HashHandler.getInstance().setHash(this.getSelectedPath());
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
