define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/UserPermissionsEditor.html'],
	function (RenderedView, Util, Link, tmpl) {
	
		function permissionsAsMap(permissions) {
			var map = {}
			_.each(permissions,function(perm) {
				map[perm.permission] = true;
			});
			return map;
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize : function() {
				this.loadUser(this.options.user);
			},
			
			setProject : function(project) {
				this.project = project;
			},
			
			loadUser : function(user) {
				this.user = user;
				
				var link = Link.getInstance().getAccessTypes;
				if (!link.isDataReady()) {
					var self = this;
					link.asap(function() {
						self.loadUser(user);
					});
					return;
				}
				if (user == null) return; //TODO load empty state
				
				var permissionMap = permissionsAsMap(user.userPermissions);
				
				this.model = {user:user, userPermissionMap:permissionMap, permissions:Link.getInstance().getAccessTypes.getData()};
				
				this.render();
				
				$(".remove").click($.proxy(this.removeUserClicked,this));
				
				$("input",this.el).change($.proxy(this.checkboxChanged,this));
			},
			
			removeUserClicked : function(e) {
				e.preventDefault();
				var self = this;
				Link.getInstance().removeUser({projectId:this.project.id,userId:this.user.id},function() {
					Link.getInstance().getProjectUsers.get({projectId:self.project.id}).pull();
				});
			},
			
			checkboxChanged : function(e) {
				var $el = $(e.target);
				var perm = $el.val();
				var value = $el.is(":checked");
				
				Link.getInstance().setPermission({projectId:this.project.id, userId:this.model.user.id, permission:perm, value:value},$.proxy(this.permissionSaveSuccess,this));
			},
			
			permissionSaveSuccess : function() {
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
