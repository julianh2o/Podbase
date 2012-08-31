define(
	['view/RenderedView', 'util/Util', 'view/ImageAttribute', 'text!tmpl/ImageDetails.html'],
	function (RenderedView, Util, ImageAttribute, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.refresh();
				
				this.$body = $(".attributes",this.el);
				
				this.loadDetails(this.options.file);
			},
			
			loadDetails : function(file) {
				this.$body.empty();
				this.file = file;
				if (!file) return;
				
				$.post("@{ImageBrowser.fetchInfo()}", {projectId:this.projectId,path:file.path}, $.proxy(this.imageDetailsLoaded,this),'json');
			},
			
			imageDetailsLoaded : function(attributes) {
				var self = this;
				_.each(attributes,function(attr) {
					var attrObj = new ImageAttribute({dbo:attr});
					self.$body.append(attrObj.el);
				});
			},
			
			refresh : function() {
				this.render();
				
				$(".add",this.el).click($.proxy(this.createAttribute,this));
			},
				
			createAttribute : function() {
				var attribute = prompt("Attribute name");
				if (!attribute || attribute == "") return false;
				$.post("@{ImageBrowser.createAttribute()}", {path:this.file.path,attribute:attribute,value:""},$.proxy(this.addAttribute,this),'json');
				return false;
			},
			
			addAttribute : function(attribute) {
				var imageAttribute = new ImageAttribute({dbo:attribute});
				this.$body.append(imageAttribute.el);
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
