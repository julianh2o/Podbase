require(
	['view/Viewer', 'util/Prototypes'],
	function(Viewer, Prototypes) {
		$(document).ready(function() {
			var main = new Viewer(window.viewer);
			$(document.body).append(main.el);
			main.doResize();
		});
	}
);
