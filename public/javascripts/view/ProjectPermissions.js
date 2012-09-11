define(
	['view/RenderedView', 'view/UserList', 'view/UserPermissionsEditor', 'util/Util', 'data/Link', 'text!tmpl/ProjectPermissions.html'],
	function (RenderedView, UserList, UserPermissionsEditor, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.$users = $(".users",this.el);
				this.$permissions = $(".permissions",this.el);
				
				this.userList = new UserList();
				this.$users.append(this.userList.el);
				
				this.userPermissionsEditor = new UserPermissionsEditor();
				this.$permissions.append(this.userPermissionsEditor.el);
				
				//$("html").on("ProjectSelected",$.proxy(this.projectSelected,this));
				
				$(this.userList).on("UserSelected",$.proxy(this.userSelected,this))
					.on("UserSelected",Util.debugEvent);
				
				this.setProject(this.options.project);
			},
			
			setProject : function(project) {
				this.project = project;
				this.userPermissionsEditor.setProject(project);
				
				Link.getInstance().projectUsers.get({projectId:this.project.id}).asap($.proxy(this.usersLoaded,this));
			},
			
			usersLoaded : function(link) {
				var users = link.getData();
				this.userList.setUsers(users);
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