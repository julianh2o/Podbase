define(
	['view/RenderedView', 'text!tmpl/TabModule.html'],
	function (RenderedView, tmpl) {
		
		function generateRandomString() {
			return 'xxxx-xxxx-xxxx-xxxx-xxxx-xxxx-xxxx'.replace(/[x]/g, function(c) {
			    var r = Math.random()*16|0;
			    return r.toString(16);
			});
		}
		
		return RenderedView.extend({
			template: _.template( tmpl ),
		
			initialize: function(){
				this.model = {};
				this.model.tabs = this.options.tabs;
				
				_.each(this.model.tabs,function(tab) {
					if (!tab.id) tab.id = generateRandomString();
				});
				
				this.render();
				
				var self = this;
				_.each(this.model.tabs,function(tab) {
					$("#"+tab.id,self.el).empty().append(tab.content.el || tab.content);
				});
				
				
				$("li a",this.el).click(function(e) {
					e.preventDefault();
					$(this).tab('show');
				});
				
				$("li a",this.el).first().tab("show");
				$(".tab-pane",this.el).first().addClass("active");
			},
			
			select : function(id) {
				$("#"+id,this.el).tab("show");
			}
		});
	}
)