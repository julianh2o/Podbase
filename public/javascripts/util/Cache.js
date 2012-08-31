define(
	[],
	function () {
		var This = function() {
			this.data = new Array();
		}
		
		$.extend(This.prototype,{
			put : function(key, value) {
				this.data[key] = value;
			},

			get : function(key) {
				return this.data[key];
			},

			has : function(key) {
				return this.data[key] != undefined;
			},

			clear : function(key) {
				this.data[key] = undefined;
			},
			
			purge : function() {
				this.data = new Array();
			}
		});
		
		return This;
	}
)