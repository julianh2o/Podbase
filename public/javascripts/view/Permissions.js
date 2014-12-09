define(
	['view/RenderedView', 'view/UserList', 'view/UserPermissionsEditor', 'util/Util', 'data/Link', 'text!tmpl/Permissions.html'],
	function (RenderedView, UserList, UserPermissionsEditor, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.modelObject = this.options.model;
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
				
				Link.getCurrentUser().asap($.proxy(function() {
					this.setModel(this.options.model);
				},this));
				
			},
			
			addUserClicked : function(e) {
				e.preventDefault();
				var email = prompt("Enter the user's email address");
				var self = this;
				if (!email) return;
				Link.addUserByEmail({modelId:this.modelObject.id,email:email}).post().done(function() {
					Link.getListedUsers({modelId:self.modelObject.id}).pull();
				}).fail(function(data) {
					consolelog(data);
					alert("User not found!");
				});
			},
			
			setModel : function(modelObject) {
				this.modelObject = modelObject;
				this.userPermissionsEditor.setModel(modelObject);
				
				Link.loadAll([
	                ["getUserAccess",{modelId:this.modelObject.id,userId:Link.getCurrentUser().getData().id}],
	                ["getListedUsers",{modelId:this.modelObject.id}]
	                ],$.proxy(this.usersLoaded,this),true);
			},
			
			usersLoaded : function() {
				var users = Link.getListedUsers(this.modelObject.id).getData();
				this.userList.setUsers(users);
				this.userPermissionsEditor.loadUser(null);
			},
			
			userSelected : function(event,userId,user) {
				Link.getUserAccess(this.modelObject.id,user.id).invalidate().loadOnce($.proxy(function(link) {
					var currentUser = Link.getCurrentUser().getData();
					var owner = _.groupBy(Link.getUserAccess(this.modelObject.id,currentUser.id).getData(),"name")["OWNER"] != null;
					var canEditPermissions = true;
					if (currentUser.id == user.id) canEditPermissions = false;
					if (user.email == "guest") canEditPermissions = false;
					if (owner) canEditPermissions = true;
					if (currentUser.email == "root") canEditPermissions = true;
					this.userPermissionsEditor.loadUser(user,canEditPermissions);
				},this));
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
