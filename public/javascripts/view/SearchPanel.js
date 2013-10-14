define(
	['view/RenderedView', 'data/Link', 'util/Util', 'text!tmpl/SearchResults.html', 'text!tmpl/SearchPanel.html'],
	function (RenderedView, Link, Util, resultsTmpl, tmpl) {
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			resultsTemplate: _.template( resultsTmpl ),
			
			initialize: function(options){
				this.project = options.project;
				
				this.render();
				
				this.$searchField = $(".image-search-field",this.el).on("keyup keydown keypress",$.proxy(this.fieldEvent,this));
				this.$resultsPanel = $(".results-panel",this.el).hide();
				
				this.$searchField.on("focus",$.proxy(this.showResults,this));
				
				this.query = null;
				this.results = [];
				this.timer = null;
			},
			
			fieldEvent : function() {
				console.log("field event!");
				if (this.timer != null) clearTimeout(this.timer);
				this.timer = setTimeout($.proxy(this.queryUpdated,this),500);
			},
			
			queryUpdated : function() {
				console.log("query updated");
				this.timer = null;
				
				var val = this.$searchField.val();
				
				this.query = val;
				
				if (val == "") {
					this.results = [];
					this.closeResults();
					return;
				}
				
				Link.doSearch(this.project.id,val).post({
					success:$.proxy(this.resultsReceived,this),
					error:$.proxy(this.searchError,this)
				});
			},
			
			searchError : function() {
				this.$searchField.css("color","red");
			},
			
			resultsReceived : function(results) {
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