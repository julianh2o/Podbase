define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ImageAttribute.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.attributeName = options.attr;
				this.link = options.link;
				
				this.link.asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var values = this.link.getData("byAttribute")[this.attributeName];
				
				this.model = {name:this.attributeName, values:values};
				this.render();
				
				$(".value",this.el).click($.proxy(this.triggerEditValue,this));
				//this.$attribute.dblclick($.proxy(this.triggerEditAttribute,this));
				
				$(this.el).find(".delete").click($.proxy(this.handleDelete,this));
				//this.$attribute.toggleClass("novalue",this.dbo.id == undefined);
			},
			
			triggerEditAttribute : function() {
				console.log("editing attribute");
			},
			
			triggerEditValue : function(e) {
				e.stopPropagation();
				
				var $el = $(e.target).closest(".value");
				$el.unbind("click");
				
				var val = $el.data("value");
				
				var text = $("<textarea class='seamless' rows='auto'/>");
				text.val(val);
				
				$el.empty().append(text);
				text.autosize();
				text.bind("blur", $.proxy(this.handleValueBlur,this));
				
				var self = this;
				text.bind("keydown",function(e) {
					if (e.keyCode == 13) {
						self.handleValueBlur($el);
					}
				});
				
				text.focus();
				text.select();
			},
			
			handleValueBlur : function(arg) {
				var $el = arg.target ? $(event.target).closest(".value") : arg;
				
				var value = $("textarea",this.el).val();
				var oldValue = $el.data("value");
				
				if (value == oldValue || value == "") {
					this.refresh();
					return;
				};
				
				var id = $el.data("id");
				
				var link = this.link;
				Link.updateImageAttribute(id,value).post(function() {
					link.pull();
				});
			},
				
			handleDelete : function() {
				if (!confirm("Really delete this?")) return;
				if (this.dbo.id == undefined) return;
				Link.deleteAttribute({attributeId:this.dbo.id}).post();
				if (this.dbo.templated) {
					this.dbo.id = undefined;
					this.dbo.value = undefined;
					this.refresh();
				} else {
					$(this.el).remove();
				}
			},
			

			saveAttribute : function(event) {
//					Link.createAttribute({path:browser.selectedFile.path,attribute:this.dbo.attribute,value:this.value}).post(function(attribute) {
//						var templated = that.dbo.templated;
//						that.dbo = attribute;
//						that.dbo.templated = templated;
//						that.refresh();
//					});
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
