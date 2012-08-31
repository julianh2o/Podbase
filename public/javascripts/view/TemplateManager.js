define(
	['view/RenderedView', 'util/HashHandler', 'util/Util', 'util/Cache', 'data/Link', 'view/TemplateChooser', 'view/TemplateList', 'view/TemplateEditor', 'text!tmpl/TemplateManager.html'],
	function (RenderedView, HashHandler, Util, Cache, Link, TemplateChooser, TemplateList, TemplateEditor, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				this.render();
				
				this.$templates = $(".templates",this.el);
				this.$editor = $(".editor",this.el);
				
				this.templateList = new TemplateList();
				this.$templates.append(this.templateList.el);
				
				// SELECT FIRST TEMPLATE
				if (this.templateList.getSelectedTemplate() == null) {
					this.templateList.selectIndex(0);
				}
				
				this.templateEditor = new TemplateEditor({project:this.project});
				this.$editor.append(this.templateEditor.el);
				
				$(this.templateList).on("TemplateSelected",$.proxy(this.templateSelected,this))
					.on("TemplateSelected",Util.debugEvent);
				
				this.setProject(this.options.project);
			},
			
			setProject : function(project) {
				this.templateList.setProject(project);
			},
			
			templateSelected : function(e,templateId,template) {
				this.templateEditor.setTemplate(template);
			},
			
			returnStatus : function(response) {
				
			},
			
			addAttribute : function() {
				$.post("@{TemplateController.addAttribute()}",{templateId:that.template.id,name:""},that.handleNewAttribute,'json');
			},
			
			handleNewAttribute : function(attribute) {
				var attrs = that.template.attributes;
				attrs[attrs.length] = attribute;
				that.renderTemplate(that.template);
			},
			
			findAttribute : function(id) {
				var attributes = this.template.attributes;
				for (var i=0; i<attributes.length; i++) {
					var attr = attributes[i];
					if (attr.id == id) return attr;
				}
				return null;
			},
			
			handleBlur : function(event) {
				var id = event.data.attribute.id;
				var text = event.currentTarget.value;
				
				var attribute = that.findAttribute(id);
				attribute.name = text;
				
				$.post("@{TemplateController.updateAttribute()}",{id:id,name:text},that.returnStatus,'json');
			},
			
			handleSortChange : function(event,ui) {
				var ids = Array();
				var entries = $("#templateinfo .body .entry");
				entries.each(function() {
					ids[ids.length] = $(this).data("template").id;
				});
				$.post("@{TemplateController.updateAttributeOrder()}",{templateId:that.template.id,ids:ids},that.returnStatus,'json');
			},
			
			handleRemoveAttribute : function(event) {
				var entry = $(event.currentTarget).parents(".entry");
				var attribute = event.data.attribute;
				$.post("@{TemplateController.removeAttribute()}",{id:attribute.id},new function(a,b,c) {
					that.template.attributes.remove(attribute);
					entry.remove();
				},'json');
			},
			
			renderTemplate : function(template) {
				var container = $("#attributes");
				container.empty();
				var attributes = template.attributes;
				
				function sortAttributes(a,b) {
					return a.sort - b.sort
				}
				
				function makeEntry() {
					var entry = $("<div class='entry ui-state-default ui-corner-all' />")
					entry.append("<div class='cell'><span class='handle ui-icon ui-icon-arrowthick-2-n-s'></span></div>")
					entry.append("<div class='cell'><input type='text' name='name' class='seamless' /></div>");
					entry.append("<div class='cell'><span class='remove ui-icon ui-icon-closethick'></div>");
					return entry;
				}
				
				attributes = attributes.sort(sortAttributes);
				$.each(template.attributes,function() {
					var entry = makeEntry();
					entry.data("template",this);
					var name = entry.find("input[name='name']").val(this.name);
					name.bind("blur",{attribute:this,element:name},that.handleBlur);
					entry.find(".remove").bind("click",{attribute:this},that.handleRemoveAttribute);
					
					container.append(entry);
				});
				$("#addattribute").show();
				container.sortable({ handle: '.handle',stop: this.handleSortChange});
				container.disableSelection();
			},
			
			
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
