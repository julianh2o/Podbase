define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'view/TemplateChooser', 'text!tmpl/FileBrowser.html'],
	function (RenderedView, HashHandler, Util, Cache, TemplateChooser, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.$browser = $(".browser",this.el);
				this.$path = $(".path",this.el);
				
				this.path = this.options.path;
				this.directoryCache = new Cache();
				this.selectedFile = null;
				
				this.loadPath(this.path);
			},
			
			setProject : function(projectId) {
				this.projectId = projectId;
				this.directoryCache.purge();
				this.loadPath("/");
			},
			
			loadPath : function(path, selectedFile) {
				path = path || "/";
				this.path = path.chopEnd("/") + "/";
				this.$path.html(this.path);
				$(this).trigger("PathChanged",[this.path]);

				HashHandler.getInstance().setHash(this.path);

				if (this.directoryCache.has(path)) {
					this.loadFiles(path, selectedFile, this.directoryCache.get(path));
				} else {
					var self = this;
					$.post("@{ImageBrowser.fetch()}", {projectId:this.projectId,path:path}, function(files) {
						self.directoryCache.put(path, files);
						self.loadFiles(path, selectedFile, files);
					}, 'json');
				}
			},

			loadFiles : function(path, selectedFile, files) {
				var that = this;
				var selectedOption = null;

				function renderOption(file) {
					var option = new Option();

					option.text = file.display + (file.isDir ? "/" : "");

					option.value = file.path;
					option.title = file.path;
					if (file.display == selectedFile) {
						option.selected = true;
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
				if (selectedOption != null)
					this.optionSelected(selectedOption);
				
				this.$browser.scrollTop(0);
			},

			getSelectedPath : function() {
				if (!this.selectedFile) return this.path;
				
				return Util.appendPaths(this.path, this.selectedFile.display);
			},

			optionSelected : function(event) {
				var $option = $(event.target);
				var file = $option.data("file");
				
				if (file == this.selectedFile) return;
				
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
