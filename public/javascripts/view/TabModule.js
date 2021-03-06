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
				this.selectedTab = this.options.selectedTab;
				this.model.tabs = this.options.tabs;
				
				_.each(this.model.tabs,function(tab) {
					if (!tab.id) tab.id = generateRandomString();
				});
				
				this.render();
				
				this.$tabs = $(this.el).children(".tabbable").children(".nav-tabs");
				this.$content = $(this.el).children(".tabbable").children(".tab-content");
				
				var self = this;
				_.each(this.model.tabs,function(tab) {
					$("#"+tab.id,self.el).empty().append(tab.content.el || tab.content);
				});
				
				$("li a",this.$tabs).click(function(e) {
					e.preventDefault();
					self.doSelectTab($(this));
				});
				
				$("li a",this.$tabs).first().tab("show");
				this.$content.children(".tab-pane").first().addClass("active");
				
				var $select = $("[href='#"+this.selectedTab+"']",this.$tabs);
				if (!$select.length) {
					$select = $("a",this.$tabs).first();
				}
				this.doSelectTab($select);
			},
			
			doSelectTab : function($el) {
				var id = $el.attr("href").substring(1);
				$el.tab('show');
				$(".tab-pane",this.el).removeClass("active");
				$("#"+id,this.el).addClass("active");
				this.selectedTab = id;
				$(this).trigger("TabSelected", [id]);
			},
			
			select : function(id) {
				this.doSelectTab($("#"+id,this.el));
			}
		});
	}
)