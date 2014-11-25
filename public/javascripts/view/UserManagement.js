define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/UserList', 'view/UserManagementPanel', 'text!tmpl/UserManagement.html'],
	function (RenderedView, Util, Link, UserList, UserManagementPanel, tmpl) {
		function validateEmail(email) { 
		    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		    return re.test(email);
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),

			initialize: function(opt) {
				this.access = opt.access;
				Link.getAllUsers().asap($.proxy(this.refresh,this));
				
				var self = this;
				$(this.el).on("click","a.delete-user",function(e) {
					var yes = confirm($(this).data("confirm"));
					var userId = $(this).data("userid");
					if (yes) {
						Link.deleteUser(userId).post().always(function() {
							Link.getAllUsers().pull();
						});
					}
					e.preventDefault();
				});
			},
			
			refresh : function() {
				this.render();
				
				if (!Util.permits(this.access,"CREATE_USERS")) {
					$(".add",this.el).hide();
				}
			
				this.users = Link.getAllUsers().getData();
				this.userList = Util.createView( $(".userlist",this.el), UserList, {users:this.users, decorator: this.userDecorator});
				this.userPanel = Util.createView( $(".management",this.el), UserManagementPanel);
				
				$(this.userList).on("UserSelected",$.proxy(this.userSelected,this));
				
				$(".add",this.el).click($.proxy(this.createUserClicked,this));
			},
			
			userDecorator : function(event,user,$el) {
				if (user.lastActive) {
					var timeSince = serverTime-moment(user.lastActive).unix();
					var text = "";
					if (timeSince < 60*2) text = "online";
					if (text != "") {
						var $decoration = $("<span class='decoration'>");
						$decoration.text(text);
						$el.append($decoration);
					}
				}
			},
			
			userSelected : function(e,userId,user) {
				this.userPanel.setUser(user);
				$(".mimic-user",this.el).toggle(Util.permits(this.access,"MIMIC_USERS") !== undefined);
				$(".delete-user",this.el).toggle(Util.permits(this.access,"DELETE_USERS") !== undefined);
			},
			
			createUserClicked : function(e) {
				var email = prompt("Enter the email address of the new user:");
				if (!validateEmail(email)) {
					alert("Invalid email address!");
					return;
				}
				
				Link.createUser({email:email}).post(function(password) {
					alert("The user has been sent an email with instructions on how to activate their account.");
					Link.getAllUsers().pull();
				});
				e.preventDefault();
			}
		});

		$.extend(This,{
		});

		return This;
	}
)

