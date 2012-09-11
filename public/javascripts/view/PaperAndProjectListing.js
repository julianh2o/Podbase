define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/PaperAndProjectListing.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getInstance().loadAll(["projects","papers"],$.proxy(this.refresh,this));
			},
			
			refresh : function() {
				this.model = {projects:Link.getInstance().projects.getData(),papers:Link.getInstance().papers.getData()};
				this.render();
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
