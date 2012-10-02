define(
	['view/RenderedView', 'view/ProjectSelect', 'view/UserPanel', 'data/Link', 'util/Util', 'text!tmpl/Header.html'],
	function (RenderedView, ProjectSelect, UserPanel, Link, Util, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options){
				this.render();
				
				this.projectSelect = Util.createView( $(".project-select",this.el), ProjectSelect);
				
				this.userPanel = Util.createView( $(".user-panel",this.el), UserPanel);
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)