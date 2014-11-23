define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/UserPermissionsEditor', 'text!tmpl/UserManagementPanel.html'],
	function (RenderedView, Util, Link, UserPermissionsEditor, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.setUser(options.user);
				Link.getCurrentUser().asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var podbase = Link.getCurrentPodbase().getData();
				
				if (!this.user) return;
				
				this.model = {user:this.user};
				this.render();
				
				var userId = this.user.id;
				$(".mimic-user",this.el).click(function() {
					Link.mimicUser(userId).post(function() {
						window.location.reload();
					});
				});
				
				var canEdit = true;
				var currentUser = Link.getCurrentUser().getData();
				
				if (currentUser.id == this.user.id) canEdit = false;
				if (this.user.email == "guest") canEdit = false;
				if (currentUser.email == "root") canEdit = true;
				this.userPermissionsEditor = Util.createView( $(".permissions",this.el), UserPermissionsEditor, {type:"user",user:this.user,modelObject:podbase, showRemove:false, canEdit:canEdit});
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