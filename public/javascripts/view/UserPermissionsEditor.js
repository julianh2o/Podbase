define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/UserPermissionsEditor.html'],
	function (RenderedView, Util, Link, tmpl) {
	
		function permissionsAsMap(permissions) {
			var map = {}
			_.each(permissions,function(perm) {
				map[perm] = true;
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
				
				if (this.user == null) {
					this.model = {user:null};
					this.render();
					return;
				}
				
				//TODO this is all just to make sure this static resource is ready, this can be done better.
				var link = Link.getAccessTypes();
				if (!link.isDataReady()) {
					var self = this;
					link.asap(function() {
						self.loadUser(user);
					});
					return;
				}
				
				Link.getUserAccess(this.project.id, this.user.id).asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var access = Link.getUserAccess(this.project.id,this.user.id).getData();
				var permissionMap = permissionsAsMap(access);
				
				this.model = {user:this.user, userPermissionMap:permissionMap, permissions:Link.getAccessTypes().getData()};
				
				this.render();
				
				$(".remove").click($.proxy(this.removeUserClicked,this));
				
				$("input",this.el).change($.proxy(this.checkboxChanged,this));
			},
			
			removeUserClicked : function(e) {
				e.preventDefault();
				var self = this;
				Link.removeUser({projectId:this.project.id,userId:this.user.id}).post(function() {
					Link.getListedUsers({projectId:self.project.id}).pull();
				});
			},
			
			checkboxChanged : function(e) {
				var $el = $(e.target);
				var perm = $el.val();
				var value = $el.is(":checked");
				
				Link.setPermission({projectId:this.project.id, userId:this.model.user.id, permission:perm, value:value}).post($.proxy(this.permissionSaveSuccess,this));
			},
			
			permissionSaveSuccess : function() {
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
