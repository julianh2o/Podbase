define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/UserPermissionsEditor.html'],
	function (RenderedView, Util, Link, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize : function() {
				this.type = this.options.type;
				this.showRemove = this.options.showRemove;
				this.modelObject = this.options.modelObject;
				this.loadUser(this.options.user,this.options.canEdit);
			},
			
			setModel : function(modelObject) {
				this.modelObject = modelObject;
			},
			
			loadUser : function(user,canEdit) {
				this.user = user;
				this.canEdit = canEdit;
				
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
						self.loadUser(user,canEdit);
					});
					return;
				}
				
				this.reload();
			},
			
			reload : function() {
				Link.getUserAccess(this.modelObject.id,this.user.id).invalidate().loadOnce($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var access = Link.getUserAccess(this.modelObject.id,this.user.id).getData();
				var permissionMap = Util.permissionsAsMap(access);
				
				var permissionOrdering = Link.getAccessTypes().getData();
				var possiblePermissions = Link.getAccessTypes().getData("byType")[this.type]
				possiblePermissions = _.groupBy(permissionOrdering,"aggregate");
				
				this.model = {user:this.user, userPermissionMap:permissionMap, permissions:possiblePermissions, showRemove:this.showRemove, canEdit:this.canEdit};
				
				this.render();
				
				$(".remove").click($.proxy(this.removeUserClicked,this));
				
				$("input",this.el).change($.proxy(this.checkboxChanged,this));
			},
			
			removeUserClicked : function(e) {
				e.preventDefault();
				var self = this;
				Link.removeUser({modelId:this.modelObject.id,userId:this.user.id}).post(function() {
					Link.getListedUsers({modelId:self.modelObject.id}).pull();
				});
			},
			
			checkboxChanged : function(e) {
				var $el = $(e.target);
				var perm = $el.val();
				var value = $el.is(":checked");
				
				if (this.modelObject) {
					Link.setPermission({modelId:this.modelObject.id, userId:this.user.id, permission:perm, value:value}).post($.proxy(this.permissionSaveSuccess,this));
				} else {
					Link.setUserPermission({userId:this.user.id, permission:perm, value:value}).post($.proxy(this.permissionSaveSuccess,this));
				}
			},
			
			permissionSaveSuccess : function() {
				this.reload();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
