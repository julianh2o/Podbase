define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/SearchResults.html', 'text!tmpl/SearchPanel.html'],
	function (RenderedView, Link, Util, resultsTmpl, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			resultsTemplate: _.template( resultsTmpl ),
			
			initialize: function(options){
				this.project = options.project;
				
				this.render();
				
				this.$busyIndicator = $(".busy-spinner",this.el);
				this.$searchField = $(".image-search-field",this.el).on("keyup keydown keypress",$.proxy(this.fieldEvent,this));
				this.$resultsPanel = $(".results-panel",this.el).hide();
				
				this.$searchField.on("focus",$.proxy(this.showResults,this));
				this.$searchReplaceLink = $(".search-replace-link",this.el);
				this.$searchReplaceLink.click($.proxy(this.showSearchReplace,this));
				
				this.$searchReplaceDialog = $('.search-replace-dialog',this.el).modal({show:false});
				this.$searchForReplaceField = this.$searchReplaceDialog.find(".search-field");
				this.$replaceField = this.$searchReplaceDialog.find(".replace-field");
				this.$searchWithin = $(".search-within",this.el);
				$(".do-search",this.el).click($.proxy(this.doSearchForReplace,this));
				$(".do-replace",this.el).click($.proxy(this.doSearchReplace,this));
				this.$searchReplaceResults = $(".search-replace-results",this.el);
				this.$searchReplaceResultTemplate = $(".search-replace-result-template",this.el);
				this.$doReplaceButton = this.$searchReplaceDialog.find(".do-replace");
				this.$replaceField.add(this.$searchForReplaceField).keyup($.proxy(function(event) {
				    if(event.keyCode == 13){
				    	this.doSearchForReplace();
				    }
				},this));
				
				this.query = null;
				this.results = [];
				this.timer = null;
				this.requestCount = 0;
				
				var self = this;
				this.access = Link.getAccess({modelId:this.project.id}).asap(function(link) {
					var dataMetadata = Util.permits(link.getData(),"EDIT_DATA_METADATA");
					var analysisMetadata = Util.permits(link.getData(),"EDIT_ANALYSIS_METADATA");
					if (!(dataMetadata && analysisMetadata)) self.$searchReplaceLink.hide();
				});
			},
			
			showSearchReplace : function() {
				this.$searchWithin.text(window.imageBrowser.getPath());
				this.$searchReplaceDialog.find("input").val("");
				this.$doReplaceButton.hide();
				this.$searchReplaceResults.hide();
				this.$searchReplaceDialog.modal("show");
			},
			
			doSearchForReplace : function() {
				var path = this.$searchWithin.text();
				var recursively = $(".search-recursively",this.el).prop("checked");
				var search = this.$searchField.val();
				var replace = this.$replaceField.val();
				
				$.post("@{ImageBrowser.attributeSearchReplace()}",{
					path : path,
					search: search,
					replace : replace,
					recursively : recursively,
					confirmReplace : false,
				},$.proxy(this.searchForReplaceResults,this));
			},
			
			doSearchReplace : function() {
				var path = this.$searchWithin.text();
				var recursively = $(".search-recursively",this.el).prop("checked");
				var search = this.$searchField.val();
				var replace = this.$replaceField.val();
				
				$.post("@{ImageBrowser.attributeSearchReplace()}",{
					path : path,
					search: search,
					replace : replace,
					recursively : recursively,
					confirmReplace : true,
				},$.proxy(this.searchReplaceComplete,this));
			},
			
			searchReplaceComplete : function() {
				$("html").trigger("ReloadImageDetails");
				this.$searchReplaceDialog.modal("hide");
			},
			
			searchForReplaceResults : function(data) {
				var $results = this.$searchReplaceResults.empty();
				var $template = this.$searchReplaceResultTemplate;
				$results.append($template.clone().addClass("header"));
				if (data.length != 0) {
					this.$searchReplaceResults.show();
					this.$doReplaceButton.show();
				} else {
					this.$searchReplaceResults.hide();
					this.$doReplaceButton.hide();
					return;
				}
				var path = this.$searchWithin.text();
				_.each(data,function(item) {
					var $element = $template.clone();
					var rel = "."+item.image.substring(path.length);
					$element.find(".relative-path").text(rel);
					$element.find(".attribute").text(item.attribute.attribute);
					$element.find(".original").html(item.beforePreview)
					$element.find(".replaced").html(item.afterPreview);
					$results.append($element);
				});
			},
			
			fieldEvent : function() {
				if (this.timer != null) clearTimeout(this.timer);
				this.timer = setTimeout($.proxy(this.queryUpdated,this),500);
			},
			
			queryUpdated : function() {
				this.timer = null;
				
				var val = this.$searchField.val();
				
				this.query = val;
				
				if (val == "") {
					this.results = [];
					this.closeResults();
					return;
				}
				
				
				this.requestCount++;
				this.$busyIndicator.show();
				Link.doSearch(this.project.id,val).post()
					.done($.proxy(this.resultsReceived,this))
					.error($.proxy(this.searchError,this));
			},
			
			searchError : function() {
				this.requestCount--;
				if (this.requestCount == 0) this.$busyIndicator.hide();
				
				this.$searchField.css("color","red");
			},
			
			resultsReceived : function(results) {
				this.requestCount--;
				if (this.requestCount == 0) this.$busyIndicator.hide();
				
				this.$searchField.css("color","black");
				
				this.results = results;
				
				this.$resultsPanel.show();
				this.$resultsPanel.empty().append(this.resultsTemplate({results:results}));
				
				$('.search-result',this.el).lipsis({location:"middle"});
				$(".close-panel",this.$resultsPanel).click($.proxy(this.closeResults,this));
			},
			
			showResults : function() {
				if (this.results.length) {
					this.$resultsPanel.show();
				}
			},
			
			closeResults : function() {
				this.$resultsPanel.hide();
				this.$resultsPanel.empty();
				this.results = [];
				this.query = "";
				$(".image-search-field").val("");
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)