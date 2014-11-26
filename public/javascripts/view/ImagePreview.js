define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ImagePreview.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.browser = this.options.browser;
				this.project = this.options.project;
				this.dataMode = this.options.dataMode;
				this.access = this.options.access;
				this.loadPreview(this.options.file);
			},
			
			loadPreview : function(file) {
				this.fileCount = null;
				
				if (file && file.length > 1) {
					this.file = null;
					this.files = file;
					this.fileCount = _.chain(this.files).pluck("isDir").filter(function(val) {return !val;}).value().length
					this.dirCount = _.chain(this.files).pluck("isDir").filter(function(val) {return val;}).value().length
				} else if (file && file.length) {
					this.file = file[0];
					this.files = file;
				} else if (file) {
					this.file = file;
					this.files = [file];
				} else {
					this.file = null;
					this.files = null;
				}
				
				this.canEdit = (this.dataMode && Util.permits(this.access,"EDIT_DATA_METADATA")) || (!this.dataMode && Util.permits(this.access,"EDIT_ANALYSIS_METADATA"));
				this.model = {file:this.file,files:this.files,fileCount:this.fileCount, dirCount:this.dirCount, access: this.access, Util: Util, dataMode: this.dataMode, canEdit: this.canEdit};
				
				this.render();
				
				this.$hoverImage = $(".full-image",this.el).hide();
				
				$(".html-textarea",this.el).focus(function() {
					var self = this;
					setTimeout(function() {
						$(self).select();
					},100);
				});
				
				this.overs = 0;
				$(".preview-image img",this.el).hover($.proxy(this.imageHoverIn,this),$.proxy(this.imageHoverOut,this));
				
				this.$setVisible = $(".set-visible",this.el);
				this.$setInvisible = $(".set-invisible",this.el);
				this.$toggleVisible = $(".toggle-visible",this.el);
				this.$toggleVisible.click($.proxy(this.toggleVisible,this));
				this.$setVisible.click($.proxy(this.toggleVisible,this,true));
				this.$setInvisible.click($.proxy(this.toggleVisible,this,false));
				
				this.$copy = $(".copy",this.el);
				this.$copy.click($.proxy(this.doCopy,this));
				
				this.$paste = $(".paste",this.el);
				this.$paste.click($.proxy(this.doPaste,this));
				
				this.$import = $(".import-data",this.el);
				this.$import.click($.proxy(this.importData,this));
				
				this.$export = $(".export-data",this.el);
				this.$export.click($.proxy(this.exportData,this));
				
				this.updateVisibleText();
				
				$(".add-to-image-set",this.el).unbind('click').click($.proxy(this.addToImageSet,this));
				
				$(".metadata-upload",this.el).fileupload({
			        dataType: 'json',
			        formData: $.proxy(function() {
						return [{name:"project.id","value":this.project.id},{name:"path","value":this.file.path},{name:"dataMode",value:this.dataMode}];
			        },this),
			        add: function(e,data) {
			        	data.submit();
			        },
			        complete: $.proxy(this.reloadImageAttributes,this)
			        });
			},
			
			setDataMode : function(mode) {
				this.dataMode = mode;
				this.loadPreview(this.files ? this.files : this.file);
			},
			
			doCopy : function(e) {
				e.preventDefault();
				this.browser.copyAttributes();
			},
			
			doPaste : function(e) {
				e.preventDefault();
				this.browser.pasteAttributes();
			},
			
			imageHoverIn : function() {
				if (this.overs == 0) {
					var src = this.$hoverImage.data("src");
					if (!this.$hoverImage.attr("src")) {
						this.$hoverImage.attr("src",src);
					}
					this.$hoverImage.show();
				}
				this.overs++;
			},
			
			imageHoverOut : function() {
				this.overs--;
				if (this.overs == 0) {
					this.$hoverImage.hide();
				}
			},
			
			importData : function() {
				Link.importFromFile(this.browser.project.id,this.file.path).post($.proxy(this.refreshFilebrowser,this));
			},
			
			exportData : function() {
				Link.exportToFile(this.file.path).post();
			},
			
			reloadImageAttributes : function() {
				$("html").trigger("ReloadImageDetails");
			},
			
			refreshFilebrowser : function() {
				this.browser.fileBrowser.reload();
			},
			
			updateVisibleText : function() {
				if (!this.file && !this.files) return;
				
				var visible = false;
				if (this.file) {
					visible = this.file.visible;
				} else if (this.files && this.fileCount) {
					var visibleCount = 0;
					_.each(this.files,function(file) {
						if (file.visible) visibleCount ++;
					});
					
					visible = visibleCount > this.files.length/2;
				}
				
				if (visible) {
					this.toggleStatus = false;
					this.$toggleVisible.text("Set invisible");
				} else {
					this.toggleStatus = true;
					this.$toggleVisible.text("Set visible");
				}
			},
			
			toggleVisible : function(visible,e) {
				if (this.file && this.file.isDir) {
					var dirName = Util.getFileName(this.file.path);
					var parent = Util.getParentDirectory(this.file.path);
					Link.setMultipleVisible(this.project.id,parent,dirName,visible).post();
				} else if (this.file) {
					var vis = visible == undefined ? !this.file.visible : visible;
					Link.setVisible(this.file.project.id,this.file.image.id,vis).post();
					this.browser.fileBrowser.reload();
					
					this.file.visible = vis;
					this.updateVisibleText();
				} else if (this.files) {
					var names = [];
					var dir = null;
					_.each(this.files,function(file) {
						names.push(Util.getFileName(file.path));
						dir = Util.getParentDirectory(file.path);
						
						if (file.isDir) return;
						file.visible = visible;
					});
					
					Link.setMultipleVisible(this.project.id,dir,names,visible).post();
					this.browser.fileBrowser.reload();
					
					this.updateVisibleText();
				}
				e.preventDefault();
			},
			
			addToImageSet : function(e) {
				var file = this.model.file;
				$("html").trigger("AddImageToCurrentSet", [file]);
				
				e.preventDefault();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
