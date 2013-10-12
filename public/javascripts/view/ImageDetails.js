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
				this.dataMode = this.options.dataMode;
				this.hideHidden = true;
				
				this.project = this.options.project;
				
				this.setFile(this.options.file);
				
				$("html").on("ReloadImageDetails",$.proxy(this.reload,this));
			},
			
			setDataMode : function(mode) {
				this.dataMode = mode;
				this.reload();
			},
			
			reload : function() {
				if (!this.file) {
					this.refresh();
					return;
				}
				
				this.link = Link.fetchInfo({projectId:this.project.id,path:this.file.path,dataMode:this.dataMode});
				this.link.invalidate().asap( $.proxy(this.imageDetailsLoaded,this));
			},
			
			setFile : function(file) {
				this.file = file;
				
				this.reload();
			},
			
			imageDetailsLoaded : function() {
				this.refresh();
			},
			
			getAttributes : function() {
				return _.filter(this.attributes,function(item) {
					return item.value != undefined && item.value != "";
				});
			},
			
			pasteAttributes : function(pasted) {
				var byAttribute = _.groupBy(this.attributes,"attribute");
				
				var attributesToWrite = [];
				var overwrite = null;
				_.each(pasted,function(item) {
					var existing = byAttribute[item.attribute];
					
					if (existing && existing.length) {
						existing = _.reject(existing,function(a) {
							return a.value == "" || a.value == item.value;
						});
					}
					
					var alreadyExists = existing && existing.length != 0;
					if (alreadyExists && overwrite == null) {
						overwrite = confirm("Overwrite existing attributes?");
					}
						
					if (!alreadyExists || overwrite) {
						attributesToWrite.push({
							attribute: item.attribute,
							value: item.value
						});
					}
				});
				
				if (overwrite == null) overwrite = true;
				
				/*
				var overwrite = false;
				if (existing.length) {
					var attributeList = _.pluck(existing,"attribute").join(", ");
					overwrite = 
					if (!overwrite) {
						var noquit = confirm("Continue with the remaining attributes?")
						if (!noquit) return;
					}
				}
				*/
				
				$(".attributes",this.el).block();
				$.post("@{ImageBrowser.pasteAttributes()}",{
					"project.id" : this.project.id,
					path : this.file.path,
					jsonAttributes: JSON.stringify(attributesToWrite),
					overwrite : overwrite,
					dataMode : this.dataMode
				},$.proxy(this.createAttributeSuccess,this));
			},
			
			refresh : function() {
				this.canEdit = (!this.dataMode && this.access["EDIT_ANALYSIS_METADATA"]) || (this.dataMode && this.access["EDIT_DATA_METADATA"]);
				this.attributes = this.link ? this.link.getData() : null;
				
				
				this.model = {file:this.file,attributes:this.attributes,canEdit:this.canEdit,dataMode:this.dataMode};
				
				this.render();
				
				$(".attributes",this.el).scroll($.proxy(this.attributesScrolled,this));
				
				if (!this.file) return;
				
				if (this.link) {
//					_.each(this.attributes,function(item) {
//						console.log(item.attribute,item.value);
//					});
//					this.link.getData("byAttribute")["rawr"][0].value = "moomoo";
//					_.each(this.attributes,function(item) {
//						console.log(item.attribute,item.value);
//					});
					
					this.$body = $(".attributes",this.el);
					var self = this;
					_.each(this.link.getData("byAttribute"),function(attributes,key) {
						self.addAttribute(key,attributes);
					});
				}
				
				if (this.attributes && !this.attributes.length) {
					this.$body.append("<div class='no-selection'>This image has no attributes</div>");
				}
				
				$(".add",this.el).click($.proxy(this.createAttribute,this));
				
				$(".toggle-hidden",this.el).click($.proxy(this.toggleHiddenAttributes,this));
				
				this.$body.toggleClass("show-hidden",!this.hideHidden);
				
				if (this.scrollLocation) {
					$(".attributes").scrollTop(this.scrollLocation);
				}
			},
			
			attributesScrolled : function(e) {
				var $el = $(".attributes",this.el);
				this.scrollLocation = $el.scrollTop();
			},
			
			toggleHiddenAttributes : function(e) {
				e.preventDefault();
				this.hideHidden = !this.hideHidden;
				this.$body.toggleClass("show-hidden",!this.hideHidden);
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
			
			addAttribute : function(attributeName,attributes) {
				if (!this.dataMode) {
					attributes = _.reject(attributes,function(item) {
						var res = item.data && item.linkedAttribute !== undefined;
						return res;
					});
				}
				if (attributes.length == 0) return;
				var hidden = attributes[0].hidden;
				var imageAttribute = new ImageAttribute({
					canEdit: this.canEdit,
					attr:attributeName,
					attrs:attributes,
					hidden:hidden,
					link:this.link,
					projectId: this.project.id,
					path: this.file.path,
					dataMode: this.dataMode
				});
				
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
