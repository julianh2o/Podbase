define(
	['view/RenderedView', 'util/Util', 'text!tmpl/UserList.html'],
	function (RenderedView, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.users = this.options.users;
				
				if (this.options.decorator) $(this).on("DecorateUser",this.options.decorator);
				if (this.users) this.setUsers(this.users);
			},
			
			setUsers : function(users) {
				this.model = {users:users};
				this.render();
				
				_.each($(".user",this.el),$.proxy(function(el) {
					var $el = $(el);
					var user = this.model.users[$el.data("index")];
					$(this).trigger("DecorateUser",[user,$el]);
				}),this);
				
				$(".user",this.el).click($.proxy(this.userClicked,this));
			},
			
			userClicked : function(e) {
				e.preventDefault();
				var $el = $(e.target);
				this.userSelected($el);
			},
			
			userSelected : function($el) {
				$(this.el).find("li").removeClass("active");
				$el.closest("li").addClass("active");
				
				var user = this.model.users[$el.data("index")];
				this.selected = user;
				
				$(this).trigger("UserSelected",[user.id,user]);
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
