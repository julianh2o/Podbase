define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ProjectSettings.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				$("html").on("ProjectSelected",$.proxy(this.projectSelected,this));
				this.setProject(this.options.project);
			},
			
			setProject : function(project) {
				this.project = project;
				
				this.refresh();
			},
			
			refresh : function() {
				this.model = {project:this.project};
				this.render();
				
				$(".remove",this.el).click($.proxy(this.handleRemoveClicked,this));
				
				$(".add",this.el).click($.proxy(this.handleAddClicked,this));
			},
			
			handleRemoveClicked : function(e) {
				var $el = $(e.target).closest(".directory");
				
				var id = $el.data("id");
				var self = this;
				$.post("@{ProjectController.removeDirectory}",{directoryId:id},function() {
						Link.getInstance().project.get({projectId:self.project.id}).pull();
				},'json');
			},
			
			handleAddClicked : function(e) {
				var path = prompt("Enter path to directory:");
				
				if (path) {
					var self = this;
					$.post("@{ProjectController.addDirectory}",{projectId:this.project.id, path:path},function() {
						Link.getInstance().project.get({projectId:self.project.id}).pull();
					},'json');
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
