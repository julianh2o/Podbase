define(
	['view/RenderedView', 'util/Util', 'text!tmpl/ImageAttribute.html'],
	function (RenderedView, Util, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.dbo = this.options.dbo;
				this.canEdit = this.options.canEdit;
				
				this.project = this.options.project;
				this.dataMode = this.options.dataMode;
				
				this.model = {canEdit:this.canEdit};
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
					if (this.canEdit) $el.bind("click", $.proxy(this.handleValueClick,this));
				}
			},

			handleValueClick : function(event) {
				this.editmode = true;
				
				this.refreshValue();
				
				this.$value.find("input").focus();
			},

			handleValueBlur : function(event) {
				var $el = $(event.target);
				var value = $el.val();
				
				if (value == this.value || value == "") return;
				this.value = value;
				this.saveAttribute();
			},

			saveAttribute : function(event) {
				if (this.dbo.id == undefined) {
					Link.createAttribute({path:browser.selectedFile.path,attribute:this.dbo.attribute,value:this.value}).post(function(attribute) {
						var templated = that.dbo.templated;
						that.dbo = attribute;
						that.dbo.templated = templated;
						that.refresh();
					});
				} else {
					Link.updateAttribute({attributeId:this.dbo.id,value:this.value}).post();
				}
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
