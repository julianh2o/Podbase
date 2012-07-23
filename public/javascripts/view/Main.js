define(
	['text!tmpl/Main.html', 'view/TabModule', 'view/ProjectSelect', 'util/Util', 'view/ImageBrowser'],
	function (tmpl, TabModule, ProjectSelect, Util, ImageBrowser) {
		return Backbone.View.extend({
			initialize: function(){
				window.debug = true;
				this.render();
			},
			render: function(){
				var template = _.template( tmpl );
				
				this.imageBrowser = new ImageBrowser();
				this.projectSelect = new ProjectSelect();
				var tabs = [
					{
						label: "Image Browser",
						content: this.imageBrowser
					},
					{
						label: "tab 2",
						content: "ziggle"
					}
				]
				
				this.tabs = new TabModule({tabs: tabs});
				
				$(this.el).append(this.projectSelect.el);
				$(this.el).append(this.tabs.el);
				
				$("html").on("PathChanged ProjectSelected TemplateSelected TemplateChanged",Util.debugEvent);
			}
		});
	}
)