define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/FileBrowser', 'text!tmpl/ProjectSettings.html'],
	function (RenderedView, Util, Link, FileBrowser, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.project = this.options.project;
				Link.getProject({projectId:this.project.id}).asap($.proxy(this.refresh,this));
				
				this.selectedFile = null;
			},
			
			refresh : function() {
				this.project = Link.getProject({projectId:this.project.id}).getData();
				this.model = {project:this.project};
				this.render();
				
				this.filebrowser = Util.createView( $(".filebrowser",this.el), FileBrowser, {path:"/"});
				$(this.filebrowser)
					.on("PathSelected",$.proxy(this.pathSelected,this))
					.on("PathTriggered",$.proxy(this.pathTriggered,this));
				
				$(".remove",this.el).click($.proxy(this.handleRemoveClicked,this));
				
				$(".add",this.el).click($.proxy(this.addSelectedFile,this));
			},
			
			pathTriggered : function(e,path,file) {
				this.addSelectedFile();
			},
			
			pathSelected : function(e,path,file) {
				this.selectedFile = file;
			},
			
			handleRemoveClicked : function(e) {
				var $el = $(e.target).closest(".directory");
				
				var id = $el.data("id");
				var self = this;
				Link.removeDirectory({directoryId:id}).post(function() {
						Link.getProject({projectId:self.project.id}).pull();
				});
			},
			
			addSelectedFile : function() {
				var self = this;
				Link.addDirectory({projectId:this.project.id, path:this.selectedFile.path}).post(function() {
					Link.getProject({projectId:self.project.id}).pull();
				});
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
