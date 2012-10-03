define(
	['view/RenderedView', 'view/ProjectSelect', 'view/UserPanel', 'data/Link', 'util/Util', 'text!tmpl/Header.html'],
	function (RenderedView, ProjectSelect, UserPanel, Link, Util, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options){
				this.render();
				
				if (options.projectSelect) {
					this.projectSelect = Util.createView( $(".project-select",this.el), ProjectSelect);
				} else if (options.project) {
					$(".project-select",this.el).html("<div class='selected-project'>Project:<span>"+options.project.name+"</span></div>");
				} else {
					$(".project-select",this.el).css("minHeight","10px");
				}
				
				this.userPanel = Util.createView( $(".user-panel",this.el), UserPanel);
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)