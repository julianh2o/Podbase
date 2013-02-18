define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/Footer.html'],
	function (RenderedView, Link, Util, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options){
				this.render();
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)