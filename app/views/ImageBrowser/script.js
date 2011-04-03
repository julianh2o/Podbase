var ImageBrowser = function($) {
	String.prototype.endsWith = function(suffix) {
	    return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	this.Util = {
		getFileName : function(path) {
			var tokens = path.split("/");
			return tokens[tokens.length-1];
		},
		getParentDirectory : function(path) {
			if (path.endsWith("/")) {
				path = path.substr(0,path.length-2);
			}
			var index = path.lastIndexOf("/");
			return path.substr(0,index);
		},
		parseLocationHash : function() {
			var result = new Array();
			
			var hash = window.location.hash.substring(1); //remove #
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
		}
	}
	
	function Cache() {
		this.data = new Array();
		
		this.put = function(key,value) {
			this.data[key] = value;
		}
		
		this.get = function(key) {
			return this.data[key];
		}
		
		this.has = function(key) {
			return this.data[key] != undefined;
		}
		
		this.clear = function(key) {
			this.data[key] = undefined;
		}
	}
	
	function FileBrowser(select,path) {
		this.path = path;
		this.directoryCache = new Cache();
		
		this.loadPath = function(path) {
			this.path = path;
			if (this.directoryCache.has(path)) {
				this.loadFiles(path,this.directoryCache.get(path));
			} else {
				var that = this;
				$.post("@{ImageBrowser.fetch()}",{path:path},function(files) {
					that.directoryCache.put(path,files);
					that.loadFiles(path,files);
				},'json');
			}
		}
		
		this.loadFiles = function(path,files) {
			var that = this;
			var filename = Util.getFileName(path);
			var selectedOption = null;
			
			function renderOption(file) {
				var option = new Option();
				
				if (file.display) {
					option.text = file.display;
				} else {
					option.text = Util.getFileName(file.path)+(file.isDir?"/":"");
				}
				
				option.value = file.path;
				option.title = file.path;
				if (file.path == path) {
					option.selected = true;
					selectedOption = option;
				}
				$(option).data("file",file)
				$(option).bind('dblclick',{browser: that},that.optionTriggered)
				$("#browser").append(option);
			}
			
			$("#browser").empty();
			if (path != "" && path != "/") {
				var parent = {display:"../",path:Util.getParentDirectory(path),isDir:true};
				renderOption(parent);
			}
			$.each(files,function() {
				renderOption(this);
			});
			if (selectedOption != null) this.optionSelected(selectedOption);
		}
		
		var imageBrowserObject = this;
		this.selectionChanged = function(event) {
			var select = event.target;
			var option = select.options[select.selectedIndex];
			imageBrowserObject.optionSelected(option);
		}
		
		this.optionSelected = function(option) {
			window.location.hash = option.value;
			var file = $(option).data("file");
			if (!file.isDir) {
				$("#preview").hide();
				$("#preview").empty();
				$("#preview").append("<img src='/data"+file.path+"'>");
				$("#preview").show();
				
				$("#metadata").empty();
				$.post("@{ImageBrowser.fetchInfo()}",{path:file.path},function(attributes) {
					var html = "<table>";
					$.each(attributes,function(){
						html += "<tr><td>"+this.attribute+"</td><td>"+this.value+"</td></tr>";
					});
					html += "</table>";
					$("#metadata").append(html);
				},'json');
			}
		}
		select.change(this.selectionChanged);
		
		this.optionTriggered = function(event) {
			var that = event.data.browser;
			var option = event.target;
			var file = $(option).data("file");
			if (file.isDir) {
				that.loadPath(file.path);
			}
		}
	}
	
	var browser;
	function loadCurrentPath() {
		var pageParameters = Util.parseLocationHash();
		var path = pageParameters[" "];
		if (path == "") {
			path = "/";
		}
		browser.loadPath(path);
	}
	
	$(document).ready(function() {
		$(window).hashchange(function() {
			console.log("hash changed: "+location.hash);
			//loadCurrentPath();
		});
		
		browser = new FileBrowser($("#browser"),"/");
		loadCurrentPath();
	});
};
ImageBrowser(jQuery);
