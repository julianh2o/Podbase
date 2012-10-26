define(
	['view/RenderedView', 'data/Link', 'text!tmpl/ProjectSelect.html'],
	function (RenderedView, Link, tmpl) {
		return RenderedView.extend({
			template: _.template( tmpl ),
			initialize: function(){
				this.model = {};
				
				this.selectedProject = null;
				
				Link.getProjects().asap($.proxy(this.refresh,this));
			},
			
			refresh : function(link) {
				this.model.projects = link.getData();
				if (this.model.projects.length == 0) return;
				
				this.render();
				
				var p = localStorage.getItem("selectedProject");
				
				var selectedProject = null;
				
				var byId = Link.getProjects().getData("byId");
				if (byId[p]) selectedProject = byId[p];
				
				if (!selectedProject) p = null;
				
				if (!p) p = this.model.projects[0].id;
				
				this.selectProject(p);
				
				$(".project",this.el).click($.proxy(this.projectClicked,this));
			},
			
			getSelectedProject : function() {
				return Link.getProjectsData("byId")[this.selectedProject];
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
				
				var project = Link.getProjectsData("byId")[id];
				$("html").trigger("ProjectSelected", [id,project]);
			}
		});
	}
)