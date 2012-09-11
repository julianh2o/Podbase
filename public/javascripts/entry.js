require(
	['view/Main', 'util/Prototypes'],
	function(Main, Prototypes) {
		$(document).ready(function() {
			var main = new Main({projectId:window.projectId});
			$(document.body).append(main.el);
		});
	}
);
