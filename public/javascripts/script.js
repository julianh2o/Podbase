require(
	['view/Main'],
	function(Main) {
		String.prototype.endsWith = function(suffix) {
			return this.indexOf(suffix, this.length - suffix.length) !== -1;
		};
		String.prototype.startsWith = function(prefix) {
			return this.indexOf(prefix) == 0;
		};
		String.prototype.chopStart = function(prefix) {
			if (this.startsWith(prefix)) {
				return this.substring(prefix.length);
			}
			return this;
		};
		String.prototype.chopEnd = function(suffix) {
			if (this.endsWith(suffix)) {
				return this.substring(0, this.length - suffix.length);
			}
			return this;
		};
		
		$(document).ready(function() {
			var main = new Main();
			$(document.body).append(main.el);
		});
	}
);
