define(
	['view/RenderedView', 'util/Util', 'text!tmpl/ImageBrowserActionBar.html'],
	function (RenderedView, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.access = options.access;
				
				this.model = {access:this.access};
				this.render();
				
				this.dataMode = true;
				if (this.options.dataMode != undefined) this.dataMode = this.options.dataMode
				
				
				this.doDataModeSelection();
				if (!_.contains(this.access,"EDITOR")) {
					$(".data-mode li",this.el).addClass("disabled");
				} else {
					$(".data-mode a",this.el).click($.proxy(this.dataModeClicked,this));
				}
			},
			
			dataModeClicked : function(e) {
				e.preventDefault();
				var $el = $(e.target);
				$el.closest("ul").find("li").removeClass("active");
				$el.closest("li").addClass("active");
				var newState = $el.hasClass("data");
				
				if (newState != this.dataMode) {
					this.dataMode = newState;
					$(this).trigger("DataModeChanged", [this.dataMode]);
				}
			},
			
			doDataModeSelection : function() {
				$(".data-mode li",this.el).removeClass("active");
				var $selected = null;
				if (this.dataMode) {
					$selected = $(".data",this.el);
				} else {
					$selected = $(".analysis",this.el);
				}
				$selected = $selected.closest("li");
				
				$selected.addClass("active");
			}
		});
		
		
		$.extend(This,{
		});
		
		return This;
	}
)
