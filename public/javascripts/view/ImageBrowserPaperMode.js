define(
	['view/ImageBrowser', 'view/ImageSet', 'data/Link', 'util/Util', 'text!tmpl/ImageBrowserPaperMode.html'],
	function (ImageBrowser, ImageSet, Link, Util, tmpl) {
		
		var This = ImageBrowser.extend({
			template: _.template( tmpl ),
			
			initialize : function(options) {
				This.__super__.initialize.apply(this, arguments)
				
				Link.getInstance().paper.get({paperId:options.paperId}).asap($.proxy(this.paperLoaded,this));
			},
			
			paperLoaded : function(paperLink) {
				this.paper = paperLink.getData();
				
				this.$imageSet = $(".image-set",this.el);
				this.imageSetView = new ImageSet({imagesetId:this.paper.imageset.id});
				this.$imageSet.empty().append(this.imageSetView.el);
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
