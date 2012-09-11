define([], function() {
	var Link = function(url, ajaxOptions) {
		this.busy = false;
		this.data = null;
		this.resourceUrl = url;
		this.transformers = {};
		this.transformedData = null;

		this.opts = $.extend({}, {
			dataType : "json",
			context : this,
			success : this.onSuccess,
			cache : false
		}, ajaxOptions);
	};

	$.extend(Link.prototype, {
		pull : function() {
			var options = $.extend({}, this.opts, {
				type : "GET",
				url : this.resourceUrl
			});
			$.ajax(options);
		},
		save : function() {
			// var options = $.extend({},this.ajaxOptions,{
			// type: "PUT",
			// });
			// $.ajax(options);
		},
		dataReady : function(callback) {
			var self = this;
			$(this).bind("Data", function() {
				callback(self);
			});
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
		addTransformer : function(key, transformer) {
			this.transformers[key] = transformer;
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

	var VariableLink = function(url, ajaxOptions) {
		this.links = {};
		this.url = url;
		this.opts = ajaxOptions;
	}

	$.extend(VariableLink.prototype, {
		get : function(params) {
			var key = $.param(params);

			if (!this.links[key]) {
				var url = this.url;
				_.each(params, function(v, k) {
					url = url.replace("{" + k + "}", v);
				});
				console.log("(linkgen url) ", url);

				this.links[key] = new Link(url, this.opts);
			}

			return this.links[key];
		}
	});

	var This = function() {
		this.loadAll = function(info,callback,repeat) {
			// data = ["str",[a,{params}]
			var self = this;
			var called = false;
			
			function getLink(x) {
				if (typeof x == "string") {
					return self[x];
				} else if (x.length > 1){
					return self[x[0]].get(x[1]);
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
				if (allReady) {
					var links = _.map(info,function(x) {
						return getLink(x);
					});
					callback.apply(this,links);
				}
			}
			
			_.each(info,function(x) {
				var link = getLink(x);
				link.asap(handler);
			});
		}
		
		this.projects = new Link("@{ProjectController.getProjects}");
		this.papers = new Link("@{PaperController.getPapers}");
		this.projectPermissions = new Link("@{ProjectController.getAllProjectPermissions}");
		this.currentUser = new Link("@{Application.getCurrentUser}");
		
		this.projects.addTransformer("byId", function(data) {
			var map = {};
			_.each(data, function(item) {
				map[item.id] = item;
			});
			return map;
		});
		
		
		this.paper = new VariableLink("/ajax/paper/{paperId}");
		
		this.imageset = new VariableLink("/ajax/imageset/{imagesetId}");

		this.project = new VariableLink("/project/{projectId}");
		this.projectAccess = new VariableLink("/project/{projectId}/access");
		this.templates = new VariableLink("/project/{projectId}/templates");
		this.projectUsers = new VariableLink("/project/{projectId}/users");
	};

	$.extend(This, {
		getInstance : function() {
			if (!This.instance)
				This.instance = new This();
			return This.instance;
		}
		
	});

	return This;
})