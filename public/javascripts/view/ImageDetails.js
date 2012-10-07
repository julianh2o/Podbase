define(
	['view/RenderedView', 'util/Util', 'view/ImageAttribute', 'text!tmpl/ImageDetails.html'],
	function (RenderedView, Util, ImageAttribute, tmpl) {
		
		function clearSelection() {
		    if(document.selection && document.selection.empty) {
		        document.selection.empty();
		    } else if(window.getSelection) {
		        var sel = window.getSelection();
		        sel.removeAllRanges();
		    }
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.access = this.options.access;
				this.canEdit = $.inArray("editMetadata",this.access) >= 0;
				this.dataMode = this.options.dataMode;
				
				this.project = this.options.project;
				this.setFile(this.options.file);
				
				$(this.el).dblclick($.proxy(this.createAttribute,this));
				
				this.reload();
			},
			
			setDataMode : function(mode) {
				this.dataMode = mode;
				this.reload();
			},
			
			reload : function() {
				if (!this.file) {
					this.showEmpty();
					return;
				}
				
				$.post("@{ImageBrowser.fetchInfo()}", {projectId:this.project.id,path:this.file.path,dataMode:this.dataMode}, $.proxy(this.imageDetailsLoaded,this),'json');
			},
			
			setFile : function(file) {
				this.file = file;
				
				
				this.reload();
			},
			
			showEmpty : function() {
				var $body = $(".attributes",this.el);
				
				$body.empty();
				$body.append("<div class='no-selection'>This image has no attributes</div>");
			},
			
			imageDetailsLoaded : function(attributes) {
				this.attributes = attributes;
				
				this.refresh();
			},
			
			refresh : function() {
				this.model = {file:this.file,attributes:this.attributes,canEdit:this.canEdit,dataMode:this.dataMode};
				
				this.render();
				this.$body = $(".attributes",this.el);
				
				if (this.file) {
					var self = this;
					_.each(this.attributes,function(attr) {
						self.addAttribute(attr);
					});
				}
				
				if (this.attributes && !this.attributes.length) {
					this.$body.append("<div class='no-selection'>This image has no attributes</div>");
				}
				
				
				$(".add",this.el).click($.proxy(this.createAttribute,this));
			},
				
			createAttribute : function(e) {
				e.preventDefault();
				clearSelection();
				var attribute = prompt("Attribute name");
				if (!attribute || attribute == "") return false;
				$.post("@{ImageBrowser.createAttribute()}", {
						projectId : this.project.id,
						path : this.file.path,
						attribute : attribute,
						value : "",
						dataMode : this.dataMode
					},$.proxy(this.addAttribute,this),'json');
				return false;
			},
			
			addAttribute : function(attribute) {
				var imageAttribute = new ImageAttribute({dbo:attribute,canEdit:this.canEdit});
				this.$body.append(imageAttribute.el);
				
				$(imageAttribute.el).click($.proxy(this.selectAttribute,this));
			},
			
			selectAttribute : function(e) {
				
				//TODO fix me
				var $selected = $(e.target);
				$selected.addClass("selected");
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
