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
			
			function renderOption(file) {
				var option = new Option();
				
				if (file.display) {
					option.text = file.display;
				} else {
					option.text = Util.getFileName(file.path)+(file.isDir?"/":"");
				}
				
				option.value = file.path;
				$(option).data("file",file)
				$(option).bind('dblclick',{browser: that},that.optionTriggered)
				$("#browser").append(option);
			}
			
			$("#browser").empty();
			if (path != "") {
				var parent = {display:"../",path:Util.getParentDirectory(path),isDir:true};
				renderOption(parent);
			}
			$.each(files,function() {
				renderOption(this);
			});
		}
		
		this.selectionChanged = function(event) {
			var select = event.target;
			var option = select.options[select.selectedIndex];
			var file = $(option).data("file");
			if (!file.isDir) {
				$("#selected").hide();
				$("#selected").empty();
				$("#selected").append("<img src='/data"+file.path+"'")
				$("#selected").show();
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
	
	$(document).ready(function() {
		browser = new FileBrowser($("#browser"),"/");
		$("#button").click(function() {
			browser.loadPath($("#pathField").val());
		}).click();
	});
	
};
ImageBrowser(jQuery);
