define(
	[],
	function () {
		var This = {
			appendPaths : function(path, file) {
				path = path.chopEnd("/");
				file = file.chopStart("/");
				return path + "/" + file;
			},
			getFileName : function(path) {
				var tokens = path.split("/");
				return tokens[tokens.length - 1];
			},
			getParentDirectory : function(path) {
				if (path.endsWith("/")) {
					path = path.substr(0, path.length - 2);
				}
				var index = path.lastIndexOf("/");
				return path.substr(0, index);
			},
			parseLocationHash : function() {
				var result = new Array();

				var hash = window.location.hash.substring(1); // remove #
				var parts = hash.split("?");
				var main = parts[0];
				result[" "] = main;

				if (parts.length > 1) {
					var paramstring = parts[1];
					var params = paramstring.split("&");
					for (i in params) {
						var param = params[i];
						var kv = param.split("=");
						var key = kv[0];
						var value = null;
						if (kv.length > 1) {
							value = kv[1];
						}
						result[key] = value;
					}
				}
				return result;
			},
			debugEvent : function(e) {
				if (window.debug) {
					console.log("[ "+e.type+" ]",_.rest(arguments).join(", "));
				}
			},
			createView : function($el, View, args) {
				args = $.extend({},{el:$el},args);
				
				var instance = new View(args);
				return instance;
			}
		}
		
		return This;
	}
)