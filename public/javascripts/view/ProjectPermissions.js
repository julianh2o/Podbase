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
				
				$(".add-user",this.el).click($.proxy(this.addUserClicked,this));
				
				this.setProject(this.options.project);
			},
			
			addUserClicked : function(e) {
				e.preventDefault();
				var email = prompt("Enter the user's email address");
				var self = this;
				Link.getInstance().addUserByEmail({projectId:this.project.id,email:email},function() {
					Link.getInstance().getListedUsers.get({projectId:self.project.id}).pull();
				});
			},
			
			setProject : function(project) {
				this.project = project;
				this.userPermissionsEditor.setProject(project);
				
				Link.getInstance().getListedUsers.get({projectId:this.project.id}).asap($.proxy(this.usersLoaded,this));
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
