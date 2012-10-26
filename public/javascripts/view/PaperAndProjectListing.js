define(
	['view/RenderedView', 'view/Header', 'view/ProjectList', 'view/PaperList', 'view/UserManagement', 'util/Util', 'data/Link', 'text!tmpl/PaperAndProjectListing.html'],
	function (RenderedView, Header, ProjectList, PaperList, UserManagement, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.header = Util.createView( $(".header",this.el), Header);
				this.project = Util.createView( $(".projects",this.el), ProjectList);
				this.paper = Util.createView( $(".papers",this.el), PaperList);
				
				Link.getCurrentUser().asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var user = Link.getCurrentUser().getData();
				
				if (user.root) {
					this.userManagement = Util.createView( $(".management",this.el), UserManagement);
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
