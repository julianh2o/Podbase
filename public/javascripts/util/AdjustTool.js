define(
	['view/RenderedView','text!tmpl/AdjustTool.html'],
	function (RenderedView, tmpl) {
		//TODO dedupe from Viewer.js
		function scaleRange(n,start,end,newStart,newEnd) {
			var ratio = (n - start) / (end - start);
			return newStart + ratio * (newEnd - newStart)
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
		
			activate : function(viewer) {
				this.viewer = viewer;
				
				this.render();
				
				this.$brightness = $(".brightness",this.el);
				this.$contrast = $(".contrast",this.el);
				
				this.$brightness.change($.proxy(this.valuesUpdated,this));
				this.$contrast.change($.proxy(this.valuesUpdated,this));
				
				this.update(viewer.state);
			},
			
			update : function() {
				var state = this.viewer.state;
				this.$brightness.val(state.brightness);
				this.$contrast.val(state.contrast);
			},
			
			valuesUpdated : function() {
				this.viewer.state.brightness = this.$brightness.val();
				this.viewer.state.contrast = this.$contrast.val();
				
				this.viewer.doProcess();
			},
			
			deactivate : function() {
				this.el.empty();
			},
			
			mousedown : function(e,pos,state) {
				return false;
			},
			
			mouseup : function(e,pos,state) {
				return false;
			},
			
			mousemove : function(e,pos,state) {
				return false;
			},
			
			mousedrag : function(e,pos,origin,state,originState) {
				function round(n,dec) {
					mul = Math.pow(10,dec);
					return Math.round(n * mul) / mul
				}
				
				state.brightness = round(scaleRange(pos.y,0,state.canvasDim.y,-150,150),0);
				state.contrast = round(scaleRange(pos.x,0,state.canvasDim.x,-1,5),2);
				
				this.update(state);
				
				return "process";
			},
			
			draw : function(g) {
			}
		});
		
		return This;
	}
)
