define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ProjectList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getInstance().loadAll(["projects"],$.proxy(this.refresh,this));
			},
			
			refresh : function() {
				this.model = {projects:Link.getInstance().projects.getData()};
				this.render();
				
				$(".add",this.el).click(function() {
					var name = prompt("Enter a name for your project.");
					if (!name) return;
					
					$.post("@{ProjectController.createProject}",{name:name},function() {
						Link.getInstance().projects.pull();
					});
				});
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
