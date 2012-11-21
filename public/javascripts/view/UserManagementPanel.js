define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/UserPermissionsEditor', 'text!tmpl/UserManagementPanel.html'],
	function (RenderedView, Util, Link, UserPermissionsEditor, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.setUser(options.user);
				
			},
			
			refresh : function() {
				var podbase = Link.getCurrentPodbase().getData();
				
				if (!this.user) return;
				
				this.model = {user:this.user};
				this.render();
				
				this.userPermissionsEditor = Util.createView( $(".permissions",this.el), UserPermissionsEditor, {type:"user",user:this.user,modelObject:podbase});
			},
			
			setUser : function(user) {
				this.user = user;
				
				Link.getCurrentPodbase().loadOnce($.proxy(this.refresh,this));
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)