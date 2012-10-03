define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/UserList', 'text!tmpl/UserManagement.html'],
	function (RenderedView, Util, Link, UserList, tmpl) {
		function validateEmail(email) { 
		    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		    return re.test(email);
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),

			initialize: function() {
				Link.getInstance().users.asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				this.render();
				
				this.users = Link.getInstance().users.getData();
				this.userList = Util.createView( $(".userlist",this.el), UserList, {users:this.users});
				
				$(".add",this.el).click($.proxy(this.createUserClicked,this));
			},
			
			createUserClicked : function(e) {
				var email = prompt("Enter the email address of the new user:");
				if (!validateEmail(email)) {
					alert("Invalid email address!");
					return;
				}
				
				$.post("@{UserController.createUser}",{email:email},function(password) {
					prompt("Save this temporary password: ",password);
					Link.getInstance().users.pull();
				});
			}
		});

		$.extend(This,{
		});

		return This;
	}
)

