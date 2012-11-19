define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/UserPicker.html'],
	function (RenderedView, Link, Util, tmpl) {
		
		var frameworkId = "${frameworkId}";
		var currentUser = "${user.id}";
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				if (frameworkId != "dev") return;
				Link.getAllUsers().asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var users = Link.getAllUsers().getData();
				this.model = {users: users, currentUser: currentUser};
				this.render();
				
				$("select",this.el).change($.proxy(this.userSelected,this));
			},
			
			userSelected : function(e) {
				var $el = $(e.target);
				var userId = $el.val();
				Link.mimicUser(userId).post(function() {
					window.location.reload(true);
				});
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
