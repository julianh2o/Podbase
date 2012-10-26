define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/ImageSet.html'],
	function (RenderedView, Link, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				Link.getImageSet({imagesetId:options.imagesetId}).asap($.proxy(this.imagesetLoaded,this));
				
				$("html").on("AddImageToCurrentSet",$.proxy(this.addImageToSet,this));
			},
			
			imagesetLoaded : function(imagesetLink) {
				this.imageset = imagesetLink.getData();
				
				this.model = {imageset:this.imageset};
				this.render();
				
				$('.image',this.el).lipsis({location:"middle"});
				
				$(".remove",this.el).click($.proxy(this.removeImageFromSet,this));
			},
			
			addImageToSet : function(e,file) {
				Link.addImageToSet({imagesetId:this.imageset.id, path:file.path}).post($.proxy(this.doUpdate,this));
			},
			
			removeImageFromSet : function(e) {
				var $el = $(e.target).parents(".image");
				var path = $el.data("path");
				e.preventDefault();
				
				Link.removeImageFromSet({imagesetId:this.imageset.id, path:path}).post($.proxy(this.doUpdate,this));
			},
			
			doUpdate : function() {
				Link.getImageSet({imagesetId:this.imageset.id}).pull();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
