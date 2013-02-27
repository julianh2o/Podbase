define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ImageAttribute.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.attributeName = options.attr;
				this.hidden = options.hidden;
				this.link = options.link;
				
				//TODO come up with a better way of doing this (this is required only for createAttribute
				this.projectId = options.projectId;
				this.path = options.path;
				this.dataMode = options.dataMode;
				
				this.link.asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var values = this.link.getData("byAttribute")[this.attributeName];
				if (!values) {
					$(this.el).remove();
					return;
				}
				
				this.model = {name:this.attributeName,hidden:this.hidden, values:values};
				this.render();
				
				$(".value .delete-attribute",this.el).click($.proxy(this.triggerDeleteAttribute,this));
				
				$(".value",this.el).click($.proxy(this.triggerEditValue,this));
				//this.$attribute.dblclick($.proxy(this.triggerEditAttribute,this));
				
				$(this.el).find(".delete").click($.proxy(this.handleDelete,this));
				//this.$attribute.toggleClass("novalue",this.dbo.id == undefined);
			},
			
			triggerDeleteAttribute : function(e) {
				var $el = $(e.target).closest(".value");
				var id = $el.data("id");
				Link.deleteImageAttribute(id).post($.proxy(this.deleteComplete,this));
				e.stopPropagation();
			},
			
			deleteComplete : function() {
				this.link.pull();
			},
			
			triggerEditAttribute : function() {
				//TODO
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
				
				if (id) {
					Link.updateImageAttribute(id,value).post(link.pull);
				} else {
					Link.createAttribute(this.projectId,this.path,this.attributeName,value,this.dataMode).post(link.pull);
					
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
