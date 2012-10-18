define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/PaperList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getInstance().loadAll(["getPapers"],$.proxy(this.refresh,this),true);
			},
			
			refresh : function() {
				this.model = {papers:Link.getInstance().getPapers.getData()};
				this.render();
				
				$(".add",this.el).click(function() {
					var name = prompt("Enter a name for your paper.");
					if (!name) return;
					
					Link.getInstance().createPaper({name:name},function() {
						Link.getInstance().getPapers.pull();
					});
				});
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
