define(['data/Loader'],function(Loader) {
	var This = function() {
		/*MARKER*/
		
		this.getProjects.addTransformer("byId", function(data) {
			var map = {};
			_.each(data, function(item) {
				map[item.id] = item;
			});
			return map;
		});
		
		this.fetchInfo.addTransformer("byAttribute", function(data) {
			return _.groupBy(data,"attribute");
		});
		
		this.getAccessTypes.addTransformer("byType",function(data) {
			return _.reduce(data,function(memo,accessInfo) {
				_.each(accessInfo.type,function(type) {
					if (!memo[type]) memo[type] = [];
					memo[type].push(accessInfo);
				});
				return memo;
			},{});
		});
	};
	
	$.extend(This.prototype,{
		loadAll : function(info,callback,repeat) {
			// data = ["str", [a,{params}] ]
			var self = this;
			var called = false;
			
			function getLink(x) {
				if (typeof x == "string") {
					return self[x]();
				} else if (x.length > 1){
					return self[x[0]](x[1]);
				}
				return null;
			}
			
			function handler() {
				var allReady = true;
				_.each(info,function(x){
					var link = getLink(x);
					if (!link.isDataReady()) {
						allReady = false;
						return false;
					}
				});
				if (allReady && (!called || repeat)) {
					var links = _.map(info,function(x) {
						return getLink(x);
					});
					called = true;
					callback.apply(this,links);
				}
			}
			
			_.each(info,function(x) {
				var link = getLink(x);
				link.asap(handler);
			});
		}
	});
	
	var instance = new This();
	window.link = instance;
	return instance;
	
});

