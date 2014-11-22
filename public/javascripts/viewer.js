require(
	['view/Viewer', 'util/Prototypes', 'util/Util'],
	function(Viewer, Prototypes, Util) {
		$(document).ready(function() {
			Util.sendHeartbeat();
			setInterval(Util.sendHeartbeat,60000);
			
			var main = new Viewer(window.viewer);
			$(document.body).append(main.el);
		});
	}
);
