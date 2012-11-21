define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/PaperList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.loadAll(["getPapers","getCurrentUserPermissions"],$.proxy(this.refresh,this),true);
			},
			
			refresh : function() {
				var showAdd = _.contains(Link.getCurrentUserPermissions().getData(),"CREATE_PROJECT");
				this.model = {papers:Link.getPapers().getData(), showAdd:showAdd};
				this.render();
				
				$(".add",this.el).click(function() {
					var name = prompt("Enter a name for your paper.");
					if (!name) return;
					
					Link.createPaper({name:name}).post(function() {
						Link.getPapers().pull();
					});
				});
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
