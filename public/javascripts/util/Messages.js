define(
	['view/RenderedView', 'util/HashHandler', 'data/Link', 'util/Util'],
	function (RenderedView, HashHandler, Link, Util) {
		
		var This = RenderedView.extend({});
		
		var messages = [];
		var $el = null;
		
		function doInit() {
			$el = $("<div class='notifications top-right'></div>");
			$("body").append($el);
		}
		
		$.extend(This,{
			success : function(message) {
				if (!$el) doInit();
				$el.notify({
					message: { text: message },
					type: "bangTidy"
				}).show();
			},
		});
		
		return This;
	}
)
