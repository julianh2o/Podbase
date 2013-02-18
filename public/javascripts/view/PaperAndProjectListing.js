define(
	['view/RenderedView', 'view/Header', 'view/Footer', 'view/ProjectList', 'view/PaperList', 'view/UserManagement', 'util/Util', 'data/Link', 'text!tmpl/PaperAndProjectListing.html'],
	function (RenderedView, Header, Footer, ProjectList, PaperList, UserManagement, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.header = Util.createView( $(".header",this.el), Header);
				this.footer = Util.createView( $(".footer",this.el), Footer);
				this.project = Util.createView( $(".projects",this.el), ProjectList);
				this.paper = Util.createView( $(".papers",this.el), PaperList);
				
				Link.getCurrentUserPermissions().asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var access = Link.getCurrentUserPermissions().getData();
				
				if (_.contains(access,"MANAGE_USERS")) {
					this.userManagement = Util.createView( $(".management",this.el), UserManagement);
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
