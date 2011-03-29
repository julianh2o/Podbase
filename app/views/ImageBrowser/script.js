new function($) {
	var Util = {
		getFileName : function(path) {
			var tokens = path.split("/");
			return tokens[tokens.length-1];
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
	
	function FileBrowser(path) {
		this.path = path;
		this.directoryCache = new Cache();
		
		this.loadPath = function(path) {
			this.path = path;
			if (this.directoryCache.has(path)) {
				this.loadFiles(this.directoryCache.get(path));
			} else {
				var that = this;
				$.post("@{ImageBrowser.fetch()}",{path:path},function(files) {
					that.directoryCache.put(path,files);
					that.loadFiles(files);
				},'json');
			}
		}
		
		this.loadFiles = function(files) {
			var that = this;
			$("#browser").empty();
			
			$.each(files,function() {
				var option = new Option();
				option.text = Util.getFileName(this.path)+(this.isDir?"/":"");
				option.value = this.path;
				$(option).data("file",this)
				$(option).bind('dblclick',{browser: that},that.optionTriggered)
				$("#browser").append(option);
			});
		}
		
		this.optionTriggered = function(event) {
			var that = event.data.browser;
			var option = event.target;
			var file = $(option).data("file");
			that.loadPath(file.path);
		}
	}
	
	browser = new FileBrowser("/");
	$(document).ready(function() {
		$("#button").click(function() {
			browser.loadPath($("#pathField").val());
		}).click();
	});
	
	
	
	
}(jQuery);
