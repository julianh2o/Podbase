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
				
				if (this.selectedProject == null) {
					var p = localStorage.getItem("selectedProject");
					
					found = false;
					for (i in this.model.projects) {
						if (p == this.model.projects[i].id) found = true;
					}
					if (!found) p = null;
					
					if (!p) p = this.model.projects[0].id;
					
					this.selectProject(p);
				}
				
				$(".project",this.el).click($.proxy(this.projectClicked,this));
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
				
				$("html").trigger("ProjectSelected", [id]);
			}
		});
	}
)