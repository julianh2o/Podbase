define(
	['view/RenderedView', 'util/Util', 'view/ImageAttribute', 'text!tmpl/ImageDetails.html'],
	function (RenderedView, Util, ImageAttribute, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.access = this.options.access;
				this.canEdit = $.inArray("editMetadata",this.access) >= 0;
				
				this.loadDetails(this.options.file);
			},
			
			loadDetails : function(file) {
				this.file = file;
				if (!file) {
					this.refresh();
					return;
				}
				
				$.post("@{ImageBrowser.fetchInfo()}", {projectId:this.projectId,path:file.path}, $.proxy(this.imageDetailsLoaded,this),'json');
			},
			
			imageDetailsLoaded : function(attributes) {
				this.attributes = attributes;
				
				this.refresh();
			},
			
			refresh : function() {
				this.model = {file:this.file,attributes:this.attributes,canEdit:this.canEdit};
				
				this.render();
				this.$body = $(".attributes",this.el);
				
				if (this.file) {
					var self = this;
					_.each(this.attributes,function(attr) {
						var attrObj = new ImageAttribute({dbo:attr,canEdit:self.canEdit});
						self.$body.append(attrObj.el);
					});
				}
				
				if (this.attributes && !this.attributes.length) {
					this.$body.append("<div class='no-selection'>This image has no attributes</div>");
				}
				
				
				$(".add",this.el).click($.proxy(this.createAttribute,this));
			},
				
			createAttribute : function() {
				var attribute = prompt("Attribute name");
				if (!attribute || attribute == "") return false;
				$.post("@{ImageBrowser.createAttribute()}", {path:this.file.path,attribute:attribute,value:""},$.proxy(this.addAttribute,this),'json');
				return false;
			},
			
			addAttribute : function(attribute) {
				var imageAttribute = new ImageAttribute({dbo:attribute,canEdit:this.canEdit});
				this.$body.append(imageAttribute.el);
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
