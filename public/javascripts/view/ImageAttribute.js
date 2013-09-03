define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/ImageAttribute.html'],
	function (RenderedView, Util, Link, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.attributeName = options.attr;
				this.hidden = options.hidden;
				this.link = options.link;
				this.canEdit = options.canEdit;
				
				//TODO come up with a better way of doing this (this is required only for createAttribute
				this.projectId = options.projectId;
				this.path = options.path;
				this.dataMode = options.dataMode;
				
				this.attributes = options.attrs;
				
				this.link.asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				if (!this.attributes) {
					$(this.el).remove();
					return;
				}
				
				this.model = {name:this.attributeName,hidden:this.hidden, values:this.attributes};
				this.render();
				
				
				if (this.canEdit) {
					$(".value .delete-attribute",this.el).click($.proxy(this.triggerDeleteAttribute,this));
					
					$(".value",this.el).click($.proxy(this.triggerEditValue,this));
				} else {
					$(".delete-attribute",this.el).hide();
				}
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
				//console.log("editing attribute");
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
				var $el = arg.target ? $(arg.target).closest(".value") : arg;
				
				var value = $("textarea",this.el).val();
				var oldValue = $el.data("value");
				
				if (value == oldValue) {
					this.refresh();
					return;
				};
				
				var id = $el.data("id");
				var index = $el.data("index");
				var attribute = this.attributes[index];
				this.attributes[index].value = value;
				
				this.showLoading();
				
				if (id) {
					Link.updateImageAttribute(id,encodeURIComponent(value),this.dataMode).post().always($.proxy(this.updateAttributeComplete,this,index));
				} else {
					Link.createAttribute(this.projectId,this.path,this.attributeName,encodeURIComponent(value),this.dataMode).post().always($.proxy(this.createAttributeComplete,this,index));
				}
			},
			
			showLoading : function() {
				$(this.el).block();
			},
			
			updateAttributeComplete : function(index,data) {
				$(this.el).unblock();
				data.templated = this.attributes[index].templated;
				this.attributes[index] = data;
				this.refresh();
			},
			
			createAttributeComplete : function(index,data) {
				$(this.el).unblock();
				data.templated = this.attributes[index].templated;
				this.attributes[index] = data;
				this.refresh();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
