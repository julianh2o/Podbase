new function($) {
	
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
				$("#browser").append(that.renderFile(this));
			});
		}
		
		this.renderFile(file) {
			return "file:"+file.path+"<br/>";
		}
	}
	
	browser = new FileBrowser("/");
	$(document).ready(function() {
		$("#button").click(function() {
			browser.loadPath($("#pathField").val());
		});
	});
	
	
	
	
}(jQuery);
