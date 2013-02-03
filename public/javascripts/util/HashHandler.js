define(
	[],
	function () {
		var This = function() {
			this.internalHash = window.location.hash;
			
			this.document
			
			$(document).ready($.proxy(this.documentReady,this));
	
		};
		
		$.extend(This.prototype,{
			setHash : function(hash) {
				if (this.internalHash == hash) return;
				
				this.internalHash = hash;
				
				window.location.hash = hash;
			},
			
			replaceHash : function(hash) {
				if (this.internalHash == hash) return;
				
				this.internalHash = hash;
				window.location.replace("#"+hash);
			},
			
			documentReady : function() {
				$(window).hashchange($.proxy(this.hashChangeHandler,this));
			},
			
			hashChangeHandler : function() {
				var newHash = window.location.hash.substring(1);
				if (this.internalHash == newHash) return;

				this.internalHash = newHash;
				$(this).trigger("HashUpdate", newHash);
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