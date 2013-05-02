define(
	['view/RenderedView', 'view/UserList', 'view/UserPermissionsEditor', 'util/Util', 'data/Link', 'text!tmpl/Permissions.html'],
	function (RenderedView, UserList, UserPermissionsEditor, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.type = this.options.type;
				
				this.$users = $(".users",this.el);
				this.$permissions = $(".permissions",this.el);
				
				this.userList = new UserList();
				this.$users.append(this.userList.el);
				
				this.userPermissionsEditor = new UserPermissionsEditor({type:this.type,showRemove:true});
				this.$permissions.append(this.userPermissionsEditor.el);
				
				$(this.userList).on("UserSelected",$.proxy(this.userSelected,this))
					.on("UserSelected",Util.debugEvent);
				
				$(".add-user",this.el).click($.proxy(this.addUserClicked,this));
				
				this.setModel(this.options.model);
			},
			
			addUserClicked : function(e) {
				e.preventDefault();
				var email = prompt("Enter the user's email address");
				var self = this;
				Link.addUserByEmail({modelId:this.modelObject.id,email:email}).post(function() {
					Link.getListedUsers({modelId:self.modelObject.id}).pull();
				});
			},
			
			setModel : function(modelObject) {
				this.modelObject = modelObject;
				this.userPermissionsEditor.setModel(modelObject);
				
				Link.getListedUsers({modelId:this.modelObject.id}).asap($.proxy(this.usersLoaded,this));
			},
			
			usersLoaded : function(link) {
				var users = link.getData();
				this.userList.setUsers(users);
				this.userPermissionsEditor.loadUser(null);
			},
			
			userSelected : function(event,userId,user) {
				this.userPermissionsEditor.loadUser(user);
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
