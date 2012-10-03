define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/PaperList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getInstance().loadAll(["papers"],$.proxy(this.refresh,this));
			},
			
			refresh : function() {
				this.model = {papers:Link.getInstance().papers.getData()};
				this.render();
				
				$(".add",this.el).click(function() {
					var name = prompt("Enter a name for your paper.");
					if (!name) return;
					
					$.post("@{PaperController.createPaper}",{name:name},function() {
						Link.getInstance().papers.pull();
					});
				});
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
