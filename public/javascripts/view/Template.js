define(
	['view/RenderedView', 'text!tmpl/ImageBrowser.html'],
	function (RenderedView, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			initialize: function()  {
				
				
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)