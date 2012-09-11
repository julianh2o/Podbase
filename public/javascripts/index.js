require(
	['view/PaperAndProjectListing', 'util/Prototypes'],
	function(PaperAndProjectListing, Prototypes) {
		$(document).ready(function() {
			var view = new PaperAndProjectListing();
			$(document.body).append(view.el);
		});
	}
);
