new function($) {
	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};
	String.prototype.startsWith = function(prefix) {
		return this.indexOf(prefix) == 0;
	};
	String.prototype.chopStart = function(prefix) {
		if (this.startsWith(prefix)) {
			return this.substring(prefix.length);
		}
		return this;
	};
	String.prototype.chopEnd = function(suffix) {
		if (this.endsWith(suffix)) {
			return this.substring(0, this.length - suffix.length);
		}
		return this;
	};

	var HashHandler = (function() {
		var internalHash = window.location.hash;
		$(document).ready(function() {
			$(window).hashchange(function() {
				var newHash = window.location.hash.substring(1);
				if (internalHash == newHash) return;

				internalHash = newHash;
				$(window).trigger("hashEdited", newHash);
			});
		});

		return ({
			setHash : function(hash) {
				internalHash = hash;
				window.location.hash = hash;
			}
		})

	})();

	var Util = {
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
		}
	}

	function Cache() {
		this.data = new Array();

		this.put = function(key, value) {
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

	function FileBrowser(select, path) {
		this.templateChooser = new TemplateChooser($("#templatechooser"));
		this.path = path;
		this.directoryCache = new Cache();
		this.selectedFile = null;
		
		this.loadPath = function(path, selectedFile) {
			this.path = path.chopEnd("/") + "/";
			this.templateChooser.pathChanged(path);

			HashHandler.setHash(this.path);

			if (this.directoryCache.has(path)) {
				this.loadFiles(path, selectedFile, this.directoryCache.get(path));
			} else {
				var that = this;
				$.post("@{ImageBrowser.fetch()}", {projectId:projectId,path:path}, function(files) {
					that.directoryCache.put(path, files);
					that.loadFiles(path, selectedFile, files);
				}, 'json');
			}
		}

		this.loadFiles = function(path, selectedFile, files) {
			var that = this;
			var selectedOption = null;

			function renderOption(file) {
				var option = new Option();

				option.text = file.display + (file.isDir ? "/" : "");

				option.value = file.path;
				option.title = file.path;
				if (file.display == selectedFile) {
					option.selected = true;
					selectedOption = option;
				}
				$(option).data("file", file)
				$(option).bind('dblclick', {browser:that}, that.optionTriggered);
				$("#browser").append(option);
			}

			$("#browser").empty();
			$.each(files, function() {
				renderOption(this);
			});
			if (selectedOption != null)
				this.optionSelected(selectedOption);
		}

		var imageBrowserObject = this;
		this.selectionChanged = function(event) {
			var select = event.target;
			var option = select.options[select.selectedIndex];
			imageBrowserObject.optionSelected(option);
		}

		this.optionSelected = function(option) {
			var file = $(option).data("file");
			this.selectedFile = file;

			HashHandler.setHash(Util.appendPaths(this.path, file.display));

			if (!file.isDir) {
				this.renderImageDetails(file);
			}
		}
		
		this.clear = function() {
			$("#preview").empty();
			$("#metadata").empty();
		}
		
		this.refresh = function() {
			if (this.selectedFile) this.renderImageDetails(this.selectedFile);
		}
		
		this.renderImageDetails = function(file) {
			console.log("render called with: "+file)
			$("#preview").hide();
			this.clear();
			$("#preview").append("<a href='@{ImageViewer.index()}"+file.path+"'><img src='/data" + file.path + "?mode=thumb'></a>");
			$("#preview").show();

			var table = $("<table><tbody /><tfoot /></table>");
			$("#metadata").append(table);
			var container = table.find("tbody");

			$.post("@{ImageBrowser.fetchInfo()}", {projectId:projectId,path:file.path}, function(attributes) {
				$.each(attributes, function() {
					var at = new ImageAttribute(this, true);
					container.append(at.element);
				});
			}, 'json');

			var addnew = $("<tr><td colspan=2><a /></td></tr>")
			var a = addnew.find("a")
			a.html("Add new attribute");
			a.attr("href", "#");
			a.click(function() {
				var attribute = prompt("Attribute name");
				if (attribute == "") return false;
				$.post("@{ImageBrowser.createAttribute()}", {path:file.path,attribute:attribute,value:""},function(attribute) {
					var imageAttribute = new ImageAttribute(attribute, true);
					$("#metadata").find("tbody").append(imageAttribute.element);
				}, 'json');
				return false;
			});
			table.find("tfoot").append(addnew);
		}
		
		var browserObject = this;
		select.change(this.selectionChanged);
		this.optionTriggered = function(event) {
			var that = event.data.browser;
			var option = event.target;
			var file = $(option).data("file");
			if (file.isDir) {
				that.loadPath(file.path, null);
				browserObject.clear();
			}
		}
	}

	function ImageAttribute(dbo, editmode) {
		this.dbo = dbo;
		this.editmode = editmode;
		var that = this;
		
		this.render = function() {
			var row = $("<tr><td class='attribute'></td><td class='value'></td><td><span class='delete ui-icon ui-icon-closethick'></span></td></tr>");
			this.refresh(row);
			return row;
		}
		
		this.refresh = function(element) { //optional parameter
			if (element == undefined) element = this.element;
			
			var attr = element.find(".attribute");
			this.renderAttribute(attr, this.editmode);
			var val = element.find(".value");
			this.renderValue(val, this.editmode);
			element.find(".delete").unbind("click").click(this.handleDelete);
			
			if (this.dbo.templated) {
				element.addClass("templated");
			} else {
				element.removeClass("templated");
			}
		}
		
		this.handleDelete = function() {
			if (that.dbo.id == undefined) return;
			$.post("@{ImageBrowser.deleteAttribute()}", {id:that.dbo.id});
			if (that.dbo.templated) {
				that.dbo.id = undefined;
				that.dbo.value = undefined;
				that.refresh();
			} else {
				that.element.remove();
			}
		}

		this.renderAttribute = function(element, editmode) {
			element.html(this.dbo.attribute);
			if (this.dbo.id == undefined) {
				element.addClass("novalue");
			} else {
				element.removeClass("novalue");
			}
		}

		this.renderValue = function(element, editmode) {
			console.log("rendering dbo: ",this.dbo);
			element.unbind("click");
			if (editmode) {
				var input = $("<input class='seamless' />");
				input.val(this.dbo.value);
				element.empty().append(input);
				input.bind("blur", {that : this}, this.handleValueBlur);
			} else {
				element.html(this.dbo.value);
				element.bind("click", {that:this}, this.handleValueClick);
			}
		}

		this.handleValueClick = function(event) {
			var that = event.data.that;
			if (that.editmode) {
				that.editmode = false;
			} else {
				that.editmode = true;
			}
			that.refresh();
		}

		this.handleValueBlur = function(event) {
			var that = event.data.that;
			if (that.value == this.value || (that.value == undefined && this.value == "")) return;
			that.value = this.value;
			that.saveAttribute();
		}

		this.saveAttribute = function(event) {
			if (this.dbo.id == undefined) {
				$.post("@{ImageBrowser.createAttribute()}", {path:browser.selectedFile.path,attribute:this.dbo.attribute,value:this.value},function(attribute) {
					var templated = that.dbo.templated;
					that.dbo = attribute;
					that.dbo.templated = templated;
					that.refresh();
				}, 'json');
				
			} else {
				$.post("@{ImageBrowser.updateAttribute()}", {id:this.dbo.id,value:this.value}, function(attribute) {
				}, 'json');
			}
		}
		
		this.element = this.render();
	}
	
	function TemplateChooser(element) {
		this.element = element;
		this.templates = null;
		this.templateCache = new Cache();
		
		var that = this;
		this.loadTemplateList = function(templates) {
			that.templates = templates;
			element.empty();
			
			function addOption(text,value) {
				var option = $("<option />");
				option.html(text);
				option.attr("value",value);
				element.append(option);
				return option;
			}
			
			addOption("-- None --", "");
			
			$.each(templates, function() {
				addOption(this.name, this.id);
			});
			
		}
		
		this.handleTemplateChange = function(event) {
			var templateId = event.currentTarget.value;
			var templateAssignment = that.templateCache.get(browser.path);
			console.log(that.templateCache);
			console.log(templateAssignment);
			
			$.post("@{TemplateController.setFolderTemplate()}", {projectId:projectId,templateId:templateId,path:browser.path},that.handleTemplateForPath,'json');
			
			browser.refresh();
		}
		
		this.pathChanged = function(path) {
			if (this.templateCache.has(path)) {
				this.setSelectedTemplate(this.templateCache.get(path));
			} else {
				this.templateCache.put(path,null);
				$.post("@{TemplateController.getTemplateForPath()}", {projectId:projectId,path:browser.path},this.handleTemplateForPath, 'json');
			}
		}
		
		this.handleTemplateForPath = function(templateAssignment) {
			if (templateAssignment != null) {
				that.templateCache.put(templateAssignment.path,templateAssignment.template);
				that.setSelectedTemplate(templateAssignment.template);
			} else {
				that.setSelectedTemplate(null);
			}
		}
		
		this.setSelectedTemplate = function(template) {
			if (template == null) {
				element.val("");
			} else {
				element.val(template.id);
			}
		}
		
		element.change(this.handleTemplateChange);
		
		$.post("@{TemplateController.fetchTemplates()}", {projectId:projectId},this.loadTemplateList, 'json');
	}

	var browser;
	
	function loadCurrentPath() {
		var pageParameters = Util.parseLocationHash();
		var path = pageParameters[" "];
		if (path == "" || path == undefined) {
			path = "/";
		}
		var selectedFile = Util.getFileName(path);
		path = path.substring(0, path.lastIndexOf("/"));
		browser.loadPath(path, selectedFile);
	}

	$(document).ready(function() {
		$(window).bind('hashEdited', function(str) {
			loadCurrentPath();
		});

		browser = new FileBrowser($("#browser"), "/");
		loadCurrentPath();
	});
}(jQuery);
