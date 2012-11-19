define([], function() {
	function doArgumentReplacement(url,args) {
		var variableMatcher = /{([^}]+)}/g;
		
		var vars = [];
		var match = undefined;
		while ((match = variableMatcher.exec(url)) !== null) {
			vars.push(match[1]);
		}
		
		var i = 0;
		_.each(vars,function(v) {
			arg = args[i++];
			
			if ($.isFunction(arg)) {
				callback = arg;
				return;
			}
			
			var value = null;
			if (typeof args[0] == "object") {
				value = args[0][v];
			} else {
				value = arg;
			}
			
			url = url.replace("{"+v+"}",value);
		});
		
		return url;
	}
	
	
	var Loader = function(url,args,opts,transformers) {
		this.url = doArgumentReplacement(url,args);
		this.busy = false;
		this.data = null;
		this.transformedData = null;
		this.args = args;
		this.transformers = transformers;
		this.opts = $.extend({},opts,{
			success : this.onSuccess,
			context : this
		});
	};

	$.extend(Loader.prototype, {
		pull : function() {
			var options = $.extend({}, this.opts, {
				type : "GET",
				url : this.url
			});
			$.ajax(options);
		},
		post : function(cb) {
			var options = $.extend({}, this.opts, {
				type : "POST",
				url : this.url,
				success: cb
			});
			$.ajax(options);
		},
		dataReady : function(callback) {
			var self = this;
			$(this).bind("Data", function() {
				callback(self);
			});
		},
		invalidate : function() {
			this.data = null;
			return this;
		},
		asap : function(callback) {
			this.dataReady(callback);

			if (this.data) {
				callback(this);
				return;
			}

			if (!this.busy) {
				this.pull();
			}
		},
		loadOnce : function(callback) {
			this.one(callback);
			
			if (this.data) {
				callback(this);
				return;
			}

			if (!this.busy) {
				this.pull();
			}
		},
		one : function(callback) {
			var self = this;
			$(this).one("Data", function() {
				callback(self);
			});
		},
		getData : function(type) {
			if (type)
				return this.transformedData[type];
			return this.data;
		},
		isDataReady : function() {
			return this.data && !this.busy;
		},
		onSuccess : function(data) {
			this.data = data;
			this.runTransformers();
			$(this).trigger("Data");
		},
		runTransformers : function() {
			var self = this;
			var transformedData = {};
			_.each(this.transformers, function(transformer, key) {
				transformedData[key] = transformer(self.data);
			});
			this.transformedData = transformedData;
		}
	});
	
	var This = function(url,ajaxOptions) {
		var resourceUrl = url;
		var transformers = {};
		var instances = {};
		
		var opts = $.extend({}, {
			dataType : "json",
			cache : false
		}, ajaxOptions);
		
		function generateKey(args) {
			var key = "";
			_.each(args,function(arg) {
				if (key.length) key+= "\0";
				key += arg;
			});
			return key;
		}
		
		var Fun = function() {
			var key = JSON.stringify(arguments);
			
			var instance = instances[key];
			if (!instance) {
				instance = new Loader(resourceUrl,arguments,opts,transformers);
				instances[key] = instance;
			}
			
			return instance;
		};
		
		$.extend(Fun,{
			addTransformer : function(key, transformer) {
				transformers[key] = transformer;
			},
		});
		
		return Fun;
	}

	$.extend(This, {
	});

	return This;
})