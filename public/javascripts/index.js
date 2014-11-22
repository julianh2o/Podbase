require(
	['view/PaperAndProjectListing', 'util/Prototypes', 'util/Util'],
	function(PaperAndProjectListing, Prototypes, Util) {
		$(document).ready(function() {
			Util.sendHeartbeat();
			setInterval(Util.sendHeartbeat,60000);
			
			var view = new PaperAndProjectListing();
			$(document.body).append(view.el);
		});
	}
);
