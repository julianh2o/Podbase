define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ProjectList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.loadAll(["getProjects","getCurrentUserPermissions"],$.proxy(this.refresh,this),true);
			},
			
			refresh : function() {
				var showAdd = Util.permits(Link.getCurrentUserPermissions().getData(),"CREATE_PROJECT");
				this.model = {projects:Link.getProjects().getData(), showAdd:showAdd};
				this.render();
				
				if (!Util.permits(Link.getCurrentUserPermissions().getData(),"DELETE_PROJECT")) {
					$(".delete-project",this.el).hide();
				}
				
				$(".delete-project",this.el).click(function() {
					var id = $(this).closest("tr").data("project-id");
					var name = $(this).closest("tr").data("project-name");
					var yes = confirm("Are you sure you want to delete '"+name+"'?");
					
					if (yes) {
						Link.deleteProject(id).post(function() {
							Link.getProjects().pull();
						});
					}
				});
				
				$(".add",this.el).click(function() {
					var name = prompt("Enter a name for your project.");
					if (!name) return;
					
					Link.createProject(name).post(function() {
						Link.getProjects().pull();
					});
				});
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
