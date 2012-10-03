define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/UserList', 'view/UserManagementPanel', 'text!tmpl/UserManagement.html'],
	function (RenderedView, Util, Link, UserList, UserManagementPanel, tmpl) {
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
				this.userPanel = Util.createView( $(".management",this.el), UserManagementPanel);
				
				$(this.userList).on("UserSelected",$.proxy(this.userSelected,this));
				
				$(".add",this.el).click($.proxy(this.createUserClicked,this));
			},
			
			userSelected : function(e,userId,user) {
				console.log("user",user);
				this.userPanel.setUser(user);
			},
			
			createUserClicked : function(e) {
				var email = prompt("Enter the email address of the new user:");
				if (!validateEmail(email)) {
					alert("Invalid email address!");
					return;
				}
				
				$.post("@{UserController.createUser}",{email:email},function(password) {
					alert("The user has been sent an email with instructions on how to activate their account.");
					Link.getInstance().users.pull();
				});
			}
		});

		$.extend(This,{
		});

		return This;
	}
)

