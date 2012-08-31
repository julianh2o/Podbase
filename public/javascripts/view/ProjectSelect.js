define(
	['view/RenderedView', 'data/Link', 'text!tmpl/ProjectSelect.html'],
	function (RenderedView, Link, tmpl) {
		return RenderedView.extend({
			template: _.template( tmpl ),
			initialize: function(){
				this.model = {};
				
				this.selectedProject = null;
				
				Link.getInstance().projects.asap($.proxy(this.refresh,this));
			},
			
			refresh : function(link) {
				this.model.projects = link.getData();
				
				this.render();
				
				var p = localStorage.getItem("selectedProject");
				
				var selectedProject = Link.getInstance().projects.getData("byId")[p];
				if (!selectedProject) p = null;
				
				if (!p) p = this.model.projects[0].id;
				
				this.selectProject(p);
				
				$(".project",this.el).click($.proxy(this.projectClicked,this));
			},
			
			getSelectedProject : function() {
				return Link.getInstance().projects.getData("byId")[this.selectedProject];
			},
			
			projectClicked : function(event) {
				var $el = $(event.target);
				
				this.selectProject($el.data("project-id"));
			},
			
			selectProject : function(id) {
				this.selectedProject = id;
				
				localStorage.setItem("selectedProject",id);
				
				$(".nav > li",this.el).removeClass("active");
				
				var $el = $("a[data-project-id='"+id+"']",this.el)
				$el.parent().addClass("active");
				
				var project = Link.getInstance().projects.getData("byId")[id];
				$("html").trigger("ProjectSelected", [id,project]);
			}
		});
	}
)