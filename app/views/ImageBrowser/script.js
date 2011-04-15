var ImageBrowser = function($) {
	String.prototype.endsWith = function(suffix) {
	    return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};
	
	this.HashHandler = (function() {
		var internalHash = window.location.hash;
		$(document).ready(function() {
			$(window).hashchange(function() {
				var newHash = window.location.hash.substring(1);
				if (internalHash == newHash) return;
				
				internalHash = newHash;
				$(window).trigger("hashEdited",newHash);
			});
		});
		
		
		return ({
			setHash : function(hash) {
				internalHash = hash;
				window.location.hash = hash;
			}
		})
		
	})();

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
			HashHandler.setHash(option.value);
			
			var file = $(option).data("file");
			if (!file.isDir) {
				$("#preview").hide();
				$("#preview").empty();
				$("#preview").append("<img src='/data"+file.path+"'>");
				$("#preview").show();
				
				
				$("#metadata").empty();
				
				var table = $("<table><tbody /><tfoot /></table>");
				$("#metadata").append(table);
				var container = table.find("tbody");
				
				$.post("@{ImageBrowser.fetchInfo()}",{path:file.path},function(attributes) {
					$.each(attributes,function(){
						var at = new ImageAttribute(this,true);
						container.append(at.element);
					});
				},'json');
				
				var addnew = $("<tr><td colspan=2><a /></td></tr>")
				var a = addnew.find("a")
				a.html("Add new attribute");
				a.attr("href","#");
				a.click(function() {
					var attribute = prompt("Attribute name");
					if (attribute == "") return false;
					$.post("@{ImageBrowser.createAttribute()}",{path:file.path,attribute:attribute,value:""},function(attribute) {
						console.log(attribute);
						var imageAttribute = new ImageAttribute(attribute,true);
						$("#metadata").find("tbody").append(imageAttribute.element);
					},'json');
					return false;
				});
				table.find("tfoot").append(addnew);
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
	
	function ImageAttribute(dbo,editmode) {
		this.dbo = dbo;
		this.editmode = editmode;
		this.element = this.render();
		
		var that = this;
	}
	
	ImageAttribute.prototype = {
		render : function() {
			var row = $("<tr><td class='attribute'></td><td class='value'></td></tr>");
			var attr = row.find(".attribute");
			this.renderAttribute(attr,this.editmode);
			var val = row.find(".value");
			this.renderValue(val,this.editmode);
			return row;
		},
		
		refresh : function() {
			this.renderAttribute(this.element.find(".attribute"),this.editmode);
			this.renderValue(this.element.find(".value"),this.editmode);
		},
		
		renderAttribute : function(element,editmode) {
			element.html(this.dbo.attribute);
		},
		
		renderValue : function(element,editmode) {
			element.unbind("click");
			if (editmode) {
				var input = $("<input />");
				input.val(this.dbo.value);
				element.empty().append(input);
				input.bind("blur",{that:this},this.handleValueBlur);
			} else {
				element.html(this.dbo.value);
				element.bind("click",{that:this},this.handleValueClick);
			}
		},
	
		handleValueClick : function(event) {
			var that = event.data.that;
			if (that.editmode) {
				that.editmode = false;
			} else {
				that.editmode = true;
			}
			that.refresh();
		},
		
		handleValueBlur : function(event) {
			var that = event.data.that;
			that.value = this.value;
			that.saveAttribute();
		},
		
		saveAttribute : function(event) {
			$.post("@{ImageBrowser.updateAttribute()}",{id:this.dbo.id,value:this.value},function(attribute) {
			},'json');
		}
	}
	
	var browser;
	function loadCurrentPath() {
		var pageParameters = Util.parseLocationHash();
		var path = pageParameters[" "];
		if (path == "" || path == undefined) {
			path = "/";
		}
		browser.loadPath(path);
	}
	
	$(document).ready(function() {
		$(window).bind('hashEdited',function(str) {
			loadCurrentPath();
		});
		
		browser = new FileBrowser($("#browser"),"/");
		loadCurrentPath();
	});
};
ImageBrowser(jQuery);
