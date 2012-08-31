define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'text!tmpl/TemplateEditor.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.setTemplate(this.options.template);
			},
			
			setTemplate : function(template) {
				this.editTemplate = template;
				this.refresh();
			},
			
			refresh : function() {
				if (!this.editTemplate) return;
				
				function sortAttributes(a,b) {
					return a.sort - b.sort
				}
				this.editTemplate.attributes = this.editTemplate.attributes.sort(sortAttributes);
				
				this.model = {template:this.editTemplate};
				this.render();
				
				$(".template-entry input",this.el).on("blur",$.proxy(this.onFieldBlur,this));
				$(".remove",this.el).on("click",$.proxy(this.onDeleteClicked,this));
				$(".add",this.el).on("click",$.proxy(this.addAttribute,this));
				
				var $container = $(".template-editor",this.el);
				$container.sortable({ handle: '.handle',stop: $.proxy(this.onSortStop,this)});
				$container.disableSelection();
			},
			
			returnStatus : function(response) {
				
			},
			
			addAttribute : function() {
				$.post("@{TemplateController.addAttribute()}",{templateId:this.editTemplate.id,name:""},$.proxy(this.handleNewAttribute,this),'json');
			},
			
			handleNewAttribute : function(attribute) {
				var attrs = this.editTemplate.attributes.push(attribute);
				this.refresh();
			},
			
			getAttributeForElement : function($el) {
				return this.editTemplate.attributes[$el.closest(".template-entry").data("index")];
			},
			
			onFieldBlur : function(event) {
				var $el = $(event.target);
				var attr = this.getAttributeForElement($el);
				
				attr.name = $el.val();
				
				$.post("@{TemplateController.updateAttribute()}",{id:attr.id,name:attr.name},$.proxy(this.returnStatus,this),'json');
			},
			
			onSortStop : function(event,ui) {
				var ids = Array();
				var entries = $(".template-entry",this.el);
				var self = this;
				_.each(entries,function(entry) {
					var attr = self.getAttributeForElement($(entry));
					ids[ids.length] = attr.id;
				});
				$.post("@{TemplateController.updateAttributeOrder()}",{templateId:this.editTemplate.id,ids:ids},$.proxy(this.returnStatus,this),'json');
			},
			
			onDeleteClicked : function(event) {
				var $el = $(event.target).closest(".template-entry");
				var attribute = this.getAttributeForElement($el);
				
				var self = this;
				$.post("@{TemplateController.removeAttribute()}",{id:attribute.id},function() {
					self.editTemplate.attributes.splice($el.data("index"),1);
					self.refresh();
				},'json');
			},
			
			renderTemplate : function(template) {
			},
			
			
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
