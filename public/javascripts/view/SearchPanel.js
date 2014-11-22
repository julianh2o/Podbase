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
				$(".search-replace-link",this.el).click($.proxy(this.showSearchReplace,this));
				
				this.$searchReplaceDialog = $('.search-replace-dialog',this.el).modal({show:false});
				this.$searchField = this.$searchReplaceDialog.find(".search-field");
				this.$replaceField = this.$searchReplaceDialog.find(".replace-field");
				this.$searchWithin = $(".search-within",this.el);
				$(".do-search",this.el).click($.proxy(this.doSearchForReplace,this));
				$(".do-replace",this.el).click($.proxy(this.doSearchReplace,this));
				this.$searchReplaceResults = $(".search-replace-results",this.el);
				this.$searchReplaceResultTemplate = $(".search-replace-result-template",this.el);
				this.$doReplaceButton = this.$searchReplaceDialog.find(".do-replace");
				this.$replaceField.add(this.$searchField).keyup($.proxy(function(event) {
				    if(event.keyCode == 13){
				    	this.doSearchForReplace();
				    }
				},this));
				
				this.query = null;
				this.results = [];
				this.timer = null;
				this.requestCount = 0;
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
				this.$searchReplaceDialog.modal("show");
			},
			
			searchForReplaceResults : function(data) {
				var $results = this.$searchReplaceResults.empty();
				var $template = this.$searchReplaceResultTemplate;
				if (data.length != 0) {
					this.$searchReplaceResults.show();
					this.$doReplaceButton.show();
				} else {
					this.$searchReplaceResults.hide();
					this.$doReplaceButton.hide();
					return;
				}
				_.each(data,function(item) {
					var $element = $template.clone();
					$element.find(".relative-path").text(item.image);
					$element.find(".original").text(item.before)
					$element.find(".replaced").text(item.after);
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
				Link.doSearch(this.project.id,val).post({
					success:$.proxy(this.resultsReceived,this),
					error:$.proxy(this.searchError,this)
				});
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