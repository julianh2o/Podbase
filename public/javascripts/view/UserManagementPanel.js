define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/UserManagementPanel.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				if (options.user) this.setUser(options.user);
			},
			
			setUser : function(user) {
				this.model = {user:user};
				
				this.render();
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)