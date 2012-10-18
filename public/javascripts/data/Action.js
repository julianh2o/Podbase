define(
	['data/Link'],
	function(Link) {
		var This = function(url,ajaxOptions) {
			this.opts = ajaxOptions;
			
			var variableMatcher = /{([^}]+)}/g;
			var match = undefined;
			
			var vars = [];
			while ((match = variableMatcher.exec(url)) !== null) {
				vars.push(match[1]);
			}
			
			return function() {
				var curl = url;
				var callback = null;
				var i = 0;
				console.log("vars",vars,"args",arguments);
				var saveargs = arguments;
				_.each(vars,function(v) {
					arg = saveargs[i++];
					
					if ($.isFunction(arg)) {
						callback = arg;
						return;
					}
					
					var value = null;
					if ($.isPlainObject(saveargs[1])) {
						value = saveargs[1][v];
					} else {
						value = arg;
					}
					
					console.log("replacing ",v,value);
					
					curl = curl.replace("{"+v+"}",value);
				});
				$.ajax($.extend({},ajaxOptions,{
					url: curl,
					success: callback
				}));
			}
		}
	
		return This;
	}
);