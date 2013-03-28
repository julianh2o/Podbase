define(
	[],
	function () {
		var This = {
			isPathValid : function(path) {
				if (!path) return false;
				return !!path.match(/^\//);
			},
			assertPath : function(path) {
				if (This.isPathValid(path)) return;
				throw "Invalid path! '"+path+"'";
			},
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
				path = path.substr(0, index);
				if (path == "") path = "/";
				return path;
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
					console.log("[ "+e.type+" ]",_.rest(arguments)); //.join(", "));
				}
			},
			createView : function($el, View, args) {
				args = $.extend({},{el:$el},args);
				
				var instance = new View(args);
				return instance;
			},
			arrayToMap : function(array,key,value) {
				map = {};
				_.each(array,function(item) {
					var map_key = item[key];
					var map_val = item[value];
					map[map_key] = map_val;
				});
				return map;
			},
			permissionsAsMap : function(permissions,type) {
				var map = {}
				_.each(permissions,function(perm) {
					if (!type || _.contains(perm.type,type)) {
						map[perm.name] = perm;
					}
				});
				return map;
			}
		}
		
		return This;
	}
)