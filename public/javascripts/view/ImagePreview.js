define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ImagePreview.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.loadPreview(this.options.file);
				this.browser = this.options.browser;
			},
			
			loadPreview : function(file) {
				this.file = file;
				this.model = {file:file};
				
				this.render();
				
				this.$toggleVisible = $(".toggle-visible",this.el);
				this.$toggleVisible.click($.proxy(this.toggleVisible,this));
				
				this.updateVisibleText();
				
				$(".add-to-image-set",this.el).unbind('click').click($.proxy(this.addToImageSet,this));
			},
			
			updateVisibleText : function() {
				if (!this.file) return;
				
				if (this.file.visible) {
					this.$toggleVisible.text("Set invisible");
				} else {
					this.$toggleVisible.text("Set visible");
				}
			},
			
			toggleVisible : function(e) {
				if (this.file) {
					Link.setVisible(this.file.project.id,this.file.image.id,!this.file.visible).post();
					this.browser.fileBrowser.reload();
					
					this.file.visible = !this.file.visible;
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
