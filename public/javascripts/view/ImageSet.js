define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/ImageSet.html'],
	function (RenderedView, Link, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				Link.getInstance().imageset.get({imagesetId:options.imagesetId}).asap($.proxy(this.imagesetLoaded,this));
				
				$("html").on("AddImageToCurrentSet",$.proxy(this.addImageToSet,this));
			},
			
			imagesetLoaded : function(imagesetLink) {
				this.imageset = imagesetLink.getData();
				
				this.model = {imageset:this.imageset};
				this.render();
				
				$(".remove",this.el).click($.proxy(this.removeImageFromSet,this));
			},
			
			addImageToSet : function(e,file) {
				$.post("@{PaperController.addImageToSet}",{imagesetId:this.imageset.id, path:file.path},$.proxy(this.doUpdate,this));
			},
			
			removeImageFromSet : function(e) {
				var $el = $(e.target).parents(".image");
				var path = $el.data("path");
				e.preventDefault();
				
				$.post("@{PaperController.removeImageFromSet}",{imagesetId:this.imageset.id, path:path},$.proxy(this.doUpdate,this));
			},
			
			doUpdate : function() {
				Link.getInstance().imageset.get({imagesetId:this.imageset.id}).pull();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
