define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ProjectList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getInstance().loadAll(["getProjects"],$.proxy(this.refresh,this),true);
			},
			
			refresh : function() {
				this.model = {projects:Link.getInstance().getProjects.getData()};
				this.render();
				
				$(".add",this.el).click(function() {
					var name = prompt("Enter a name for your project.");
					if (!name) return;
					
					Link.getInstance().createProject(name,function() {
						console.log("callback called");
						Link.getInstance().getProjects.pull();
					});
				});
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
