define(
	['data/Link'],
	function(Link) {
		var This = function(url, ajaxOptions) {
			this.links = {};
			this.url = url;
			this.opts = ajaxOptions;
		}
	
		$.extend(This.prototype, {
			get : function(params) {
				var key = $.param(params);
	
				if (!this.links[key]) {
					var url = this.url;
					_.each(params, function(v, k) {
						url = url.replace("{" + k + "}", v);
					});
	
					this.links[key] = new Link(url, this.opts);
				}
	
				return this.links[key];
			}
		});
	
		return This;
	}
);