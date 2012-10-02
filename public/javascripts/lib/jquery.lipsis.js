(function($) {
    $.fn.lipsis = function(options) {
        options = $.extend({},$.fn.lipsis.options,options);
        this.each(function() {
            $.fn.lipsis.update($(this),options);
        });
    };

    $.fn.lipsis.options = {
        rows: 1,
        location: "right"
    };
    
    
	var Lipsis = function($el,o) {
		this.$el = $el;
		this.o = o;
		
		this.textNodes = null;
		this.textNodeData = null;
		this.textLength = null;
		
		this.init();
	};
	
	$.extend(Lipsis.prototype,{
		init : function() {
			this.processTextNodes();
		},
		
		computeRows : function() {
			return this.$el.height()/this.$el.css("line-height");
		},
		
		getText : function() {
			var out = "";
			for(var i=0; i<this.textNodes.length; i++) {
				out += this.textNodes[i].data;
			}
			return out;
		},
		
		getTextHeight : function() {
	        return this.$el.height(); //returns content height
		},
		
		update : function() {
	        var wordWrap = this.$el.css("word-wrap");
	        this.$el.css("word-wrap","break-word");
	
	        var text = this.getText();
	        this.doEllipsis(1,this.textLength);
	        var maxHeight = this.getTextHeight() * this.o.rows;
	
	        this.resetEllipsis();
	        
	        var currentLength = this.textLength;
	        while(this.getTextHeight() > maxHeight) {
	        	this.tryLength(currentLength);
	        	currentLength --;
	        }
	
	        this.$el.css("word-wrap",wordWrap);
		},
		
		tryLength : function(length) {
    		var remove = this.textLength - length;
    		
	    	switch(this.o.location) {
		    	case "right":
		    		this.doEllipsis(length,this.textLength);
		    		break;
		    	case "left":
		    		this.doEllipsis(0,remove);
		    		break;
		    	case "middle":
		    		var mid = this.textLength/2;
		    		var left = Math.round(mid - remove/2);
		    		var right = left + remove;
		    		this.doEllipsis(left,right);
		    		break;
	    	}
		},
		
		resetEllipsis : function() {
	        for(var i=0; i<this.textNodes.length; i++) {
                var node = this.textNodes[i];
                var nodeText = this.textNodeData[i];
                
            	node.data = nodeText;
	        }
		},
		
	    doEllipsis : function(start,end) {
	        var start_info = this.nodeAt(start);
	        var end_info = this.nodeAt(end);
	
	        for(var i=0; i<this.textNodes.length; i++) {
                var node = this.textNodes[i];
                var nodeText = this.textNodeData[i];
                
	            if (i<start_info.index || i>end_info.index) { //Nodes before target are kept in full
	            	node.data = nodeText;
	            } else if (i>start_info.index && i<end_info.index) { //Inbetweens are removed
	            	node.data = "";
	            } else if (i==start_info.index && i==end_info.index) {
	                var before = nodeText.substring(0,start_info.offset);
	                var after = nodeText.substring(end_info.offset,nodeText.length);
	                node.data = before + "..." + after;
	            } else if (i==start_info.index) {
	                var target = this.textNodes[i];
	                node.data = nodeText.substring(0,start_info.offset) + "...";
	            } else if (i==end_info.index) {
	            	node.data = nodeText.substring(end_info.offset,nodeText.length);
	            }
	        }
	    },
		
	    nodeAt : function(length) {
	        var currentLength = 0;
	        var i = 0;
	        while(true) {
	            if (i >= this.textNodeData.length) break;
	            next = currentLength + this.textNodeData[i].length;
	            if (length && next >= length) {
	                return {index:i,offset:length-currentLength,preceedingLength:currentLength};
	            }
	
	            currentLength = next;
	            i++;
	        }
	        return {index:-1,offset:this.textNodeData[i-1].length,preceedingLength:currentLength};
	    },
		
		processTextNodes : function() {
			var textNodes = [];
			var textNodeData = [];
			var textLength = 0;
			function doProcess($el) {
		        $.each($el.contents(),function() {
		            if (this.nodeType == 3) {
		            	var text = this.data;
		            	text = text.replace(/[ \t\n\r]+/g," ");
		            	textNodes.push(this);
		            	textNodeData.push(text);
		            	textLength += text.length;
		            } else {
		                doProcess($(this));
		            }
		        });
			}
			
			doProcess(this.$el);
			
			this.textNodes = textNodes;
			this.textNodeData = textNodeData;
			this.textLength = textLength;
		}
	});
	
    $.fn.lipsis.update = function($el,opts) {
    	var lipsis = new Lipsis($el,opts);
    	
    	lipsis.update();
    };
})(jQuery);
