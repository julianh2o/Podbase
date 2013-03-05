define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/SearchResults.html', 'text!tmpl/SearchPanel.html'],
	function (RenderedView, Link, Util, resultsTmpl, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			resultsTemplate: _.template( resultsTmpl ),
			
			initialize: function(options){
				this.render();
				
				this.$searchField = $(".image-search-field",this.el).on("keyup keydown keypress",$.proxy(this.queryUpdated,this));
				this.$resultsPanel = $(".results-panel",this.el).hide();
				
				this.$searchField.on("focus",$.proxy(this.showResults,this));
				
				this.query = null;
				this.results = [];
			},
			
			queryUpdated : function() {
				var val = this.$searchField.val();
				
				if (this.query == val) return;
				
				this.query = val;
				
				if (val == "") {
					this.results = [];
					this.closeResults();
					return;
				}
				
				Link.doSearch(val).post($.proxy(this.resultsReceived,this));
			},
			
			resultsReceived : function(results) {
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
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)