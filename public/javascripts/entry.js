require(
	['view/Main', 'util/Prototypes', 'util/Util'],
	function(Main, Prototypes, Util) {
		$(document).ready(function() {
			Util.sendHeartbeat();
			setInterval(Util.sendHeartbeat,60000);
			
			var main = new Main({projectId:window.projectId});
			$(document.body).append(main.el);
		});
	}
);
