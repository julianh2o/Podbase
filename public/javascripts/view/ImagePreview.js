define(
	['view/RenderedView', 'util/Util', 'text!tmpl/ImagePreview.html'],
	function (RenderedView, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.loadPreview(this.options.file);
			},
			
			loadPreview : function(file) {
				if (file) {
					this.model = {file:file};
					this.render();
				}
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
