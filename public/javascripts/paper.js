require(
	['view/PaperHome', 'util/Prototypes', 'util/Util'],
	function(PaperHome, Prototypes, Util) {
		$(document).ready(function() {
			Util.sendHeartbeat();
			setInterval(Util.sendHeartbeat,60000);
			
			var view = new PaperHome({paperId:window.paperId});
			$(document.body).append(view.el);
		});
	}
);
