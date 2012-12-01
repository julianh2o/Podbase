define(
	['view/RenderedView', 'util/Util', 'data/Link', 'view/ImageAttribute', 'text!tmpl/ImageDetails.html'],
	function (RenderedView, Util, Link, ImageAttribute, tmpl) {
		
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
				this.canEdit = $.inArray("EDITOR",this.access) >= 0;
				this.dataMode = this.options.dataMode;
				
				this.project = this.options.project;
				this.setFile(this.options.file);
				
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
				
				this.link = Link.fetchInfo({projectId:this.project.id,path:this.file.path,dataMode:this.dataMode});
				this.link.invalidate().loadOnce( $.proxy(this.imageDetailsLoaded,this));
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
			
			imageDetailsLoaded : function() {
				this.refresh();
			},
			
			refresh : function() {
				this.attributes = this.link.getData();
				
				this.model = {file:this.file,attributes:this.attributes,canEdit:this.canEdit,dataMode:this.dataMode};
				
				this.render();
				this.$body = $(".attributes",this.el);
				
				if (this.file) {
					var self = this;
					_.each(this.link.getData("byAttribute"),function(value,key) {
						self.addAttribute(key);
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
				var attribute = prompt("Add new attribute");
				if (!attribute || attribute == "") return false;
				$.post("@{ImageBrowser.createAttribute()}", {
						"project.id" : this.project.id,
						path : this.file.path,
						attribute : attribute,
						value : "",
						dataMode : this.dataMode
					},$.proxy(this.createAttributeSuccess,this),'json');
				return false;
			},
			
			createAttributeSuccess : function(attribute) {
				this.reload();
				
				var $el = $(".attributes",this.el);
				$el.scrollTop( $el.prop("scrollHeight") );
			},
			
			addAttribute : function(attributeName) {
				var imageAttribute = new ImageAttribute({attr:attributeName, link:this.link});
				this.$body.append(imageAttribute.el);
				
				$(imageAttribute.el).click($.proxy(this.selectAttribute,this));
				
			},
			
			selectAttribute : function(e) {
				var $selected = $(e.target);
				//TODO fix me
				$selected.addClass("selected");
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
