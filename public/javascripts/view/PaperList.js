define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/PaperList.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.loadAll(["getPapers","getCurrentUserPermissions"],$.proxy(this.refresh,this),true);
			},
			
			refresh : function() {
				var showAdd = Util.permits(Link.getCurrentUserPermissions().getData(),"CREATE_PAPER");
				this.model = {papers:Link.getPapers().getData(), showAdd:showAdd};
				this.render();
				
				if (!Util.permits(Link.getCurrentUserPermissions().getData(),"DELETE_PAPER")) {
					$(".delete-paper",this.el).hide();
				}
				
				$(".delete-paper",this.el).click(function() {
					var id = $(this).closest("tr").data("id");
					var name = $(this).closest("tr").data("name");
					var yes = confirm("Are you sure you want to delete '"+name+"'?");
					
					if (yes) {
						Link.deletePaper(id).post(function() {
							Link.getPapers().pull();
						});
					}
				});
				
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
