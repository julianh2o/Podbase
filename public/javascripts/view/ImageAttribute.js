define(
	['view/RenderedView', 'util/Util', 'text!tmpl/ImageAttribute.html'],
	function (RenderedView, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.dbo = this.options.dbo;
				
				this.render();
				this.$attribute = $(".attribute",this.el);
				this.$value = $(".value",this.el);
				this.editmode = false;
				
				$(this.el).find(".delete").click($.proxy(this.handleDelete,this));
				
				this.refresh();
			},
			
			refresh : function() {
				this.refreshAttribute();
				this.refreshValue();
				
				$(this.el).toggleClass("templated",this.dbo.templated);
			},
				
			handleDelete : function() {
				if (this.dbo.id == undefined) return;
				$.post("@{ImageBrowser.deleteAttribute()}", {id:this.dbo.id});
				if (this.dbo.templated) {
					this.dbo.id = undefined;
					this.dbo.value = undefined;
					this.refresh();
				} else {
					$(this.el).remove();
				}
			},

			refreshAttribute : function() {
				this.$attribute.html(this.dbo.attribute);
		
				this.$attribute.toggleClass("novalue",this.dbo.id == undefined);
			},

			refreshValue : function() {
				var $el = this.$value;
				
				$el.unbind("click");
				if (this.editmode) {
					var input = $("<input class='seamless' />");
					input.val(this.dbo.value);
					$el.empty().append(input);
					input.bind("blur", $.proxy(this.handleValueBlur,this));
				} else {
					$el.html(this.dbo.value);
					$el.bind("click", $.proxy(this.handleValueClick,this));
				}
			},

			handleValueClick : function(event) {
				this.editmode = true;
				
				this.refresh();
			},

			handleValueBlur : function(event) {
				if (this.value == this.value || (that.value == undefined && this.value == "")) return;
				this.value = this.value;
				this.saveAttribute();
			},

			saveAttribute : function(event) {
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
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
