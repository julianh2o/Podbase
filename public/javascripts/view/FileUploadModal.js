define(
	['view/RenderedView', 'util/HashHandler', 'data/Link', 'util/Util', 'text!tmpl/FileUploadModal.html', 'text!tmpl/FileUploadModalFiles.html'],
	function (RenderedView, HashHandler, Link, Util, tmpl, tmpl_files) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			filesTemplate: _.template( tmpl_files ),
			className: "modal hide fade file-upload-modal",
			
			initialize: function(options) {
				this.access = options.access;
				this.project = options.project;
				this.refresh();
			},
			
			reload : function() {
				this.reloadFiles();
			},
			
			reloadFiles : function(force) {
				var link = Link.fetchProjectPath({projectId:this.project.id,path:this.path});
				if (force) {
					$("html").trigger("FileSystemUpdated");
					link.invalidate();
				}
				link.asap($.proxy(this.filesRefreshed,this));
			},
			
			filesRefreshed : function(link) {
				this.files = link.getData();
				
				var html = this.filesTemplate({files:this.files, Util:Util});
				
				$(".files",this.el).html(html);
				
				if (!this.access.PROJECT_FILE_DELETE) {
					$(".delete-file",this.el).hide();
				} else {
					$(".delete-file",this.el).click($.proxy(this.deleteFileClicked,this));
				}
			},
			
			deleteFileClicked : function(e) {
				var $row = $(e.target).closest(".file");
				var path = $row.data("path");
				
				var yes = confirm("Are you sure you want to delete this file?\n"+path);
				
				if (yes) {
					Link.deleteFile(path).post().always($.proxy(this.uploadComplete,this));
				}
			},
			
			refresh : function() {
				$(document).bind('drop dragover', function (e) {
				    e.preventDefault();
				});
				
				this.render();
				
				if (!this.access.PROJECT_FILE_UPLOAD) {
					$(".file-upload-widget",this.el).hide();
					$(".new-directory",this.el).hide();
				} else {
					var $dropzone = $(".file-upload-widget",this.el);
				    $('.file-upload',this.el).fileupload({
				    	dropZone: $dropzone,
				        dataType: 'json',
				        formData: $.proxy(this.getData,this),
				        add: function(e,data) {
				        	data.submit();
				        },
				        complete: $.proxy(this.uploadComplete,this)
				    });
				    
				    $dropzone.bind("dragover",function(e) {
				    	$dropzone.addClass("hover");
				    });
				    
				    $dropzone.bind("dragleave drop",function(e) {
				    	$dropzone.removeClass("hover");
				    });
				    
					$(".new-directory",this.el).click($.proxy(this.createDirectoryClicked,this));
				}
			},
			
			createDirectoryClicked : function() {
				var name = prompt("Enter a name for the directory");
				if (name && name != "") {
					this.createdDirectory = this.path + "/" + name;
					Link.createDirectory(this.createdDirectory).post().always($.proxy(this.directoryCreated,this));
				}
			},
			
			directoryCreated : function() {
				this.setPath(this.createdDirectory);
				this.createdDirectory = null;
				this.reloadFiles(true);
			},
			
			uploadComplete : function() {
				this.reloadFiles(true);
			},
			
			getData : function() {
				return [{name:"path","value":this.path}];
			},
			
			setPath : function(path) {
				this.path = path;
				$(".path",this.el).html(path);
			},
			
			show : function() {
				$(this.el).modal("show");
				this.reload();
			},
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
