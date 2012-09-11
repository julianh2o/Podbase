require(
	['view/PaperHome', 'util/Prototypes'],
	function(PaperHome, Prototypes) {
		$(document).ready(function() {
			var view = new PaperHome({paperId:window.paperId});
			$(document.body).append(view.el);
		});
	}
);
