define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/UserPanel.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getInstance().getCurrentUser.asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var user = Link.getInstance().getCurrentUser.getData();
				this.model = {user:user};
				
				this.render();
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)