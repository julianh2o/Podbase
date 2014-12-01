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
				this.updateDocs = $(".update-docs",this.el).hide();
				
				Link.getCurrentUser().loadOnce($.proxy(function(link) {
					var user = link.getData();
					if (user.email == "root") this.updateDocs.show();
					this.updateDocs.click(function(e) {
						Link.updateDocs().post().done(function() {
							alert("Successfully updated docs!");
						})
						.fail(function() {
							alert("Failed to update docs");
						});
						e.preventDefault();
					});
				},this));
				
				Link.getCurrentUserPermissions().loadOnce($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var access = Link.getCurrentUserPermissions().getData();
				
				if (Util.permits(access,"MANAGE_PERMISSIONS")) {
					this.userManagement = Util.createView( $(".management",this.el), UserManagement, {access:access});
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
