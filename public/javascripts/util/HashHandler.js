define(
	[],
	function () {
		var This = function() {
			this.internalHash = window.location.hash;
			
			$(document).ready(function() {
				$(window).hashchange(function() {
					var newHash = window.location.hash.substring(1);
					if (this.internalHash == newHash) return;
	
					this.internalHash = newHash;
					$(window).trigger("HashUpdate", newHash);
				});
			});
	
		};
		
		$.extend(This.prototype,{
			setHash : function(hash) {
				this.internalHash = hash;
				window.location.hash = hash;
			}
		});
	
		
		$.extend(This,{
			getInstance: function() {
				if (!This.instance) This.instance = new This();
				return This.instance;
			}
		});
		
		return This;
	}
)