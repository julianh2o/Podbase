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
				
				this.$brightnessSlider = $(".brightness-slider",this.el).slider({
					min: -100,
					max: 100,
					step: 1,
					slide: $.proxy(this.sliderUpdated,this)
				});
				
				this.$contrastSlider = $(".contrast-slider",this.el).slider({
					min: -1,
					max: 5,
					step: .1,
					slide: $.proxy(this.sliderUpdated,this)
				});
				
				this.$brightness = $(".brightness",this.el);
				this.$contrast = $(".contrast",this.el);
				
				this.$brightness.change($.proxy(this.valuesUpdated,this));
				this.$contrast.change($.proxy(this.valuesUpdated,this));
				
				this.updateUI(viewer.state);
			},
			
			sliderUpdated : function(e,slider) {
				if (this.disableUpdates) return;
				
				this.viewer.state.brightness = this.$brightnessSlider.slider("value")
				this.viewer.state.contrast = this.$contrastSlider.slider("value");
				
				this.updateUI();
				
				this.considerProcess();
			},
			
			updateUI : function() {
				this.disableUpdates = true;
				
				var state = this.viewer.state;
				this.$brightness.val(state.brightness);
				this.$contrast.val(state.contrast);
				
				this.$brightnessSlider.slider("value",state.brightness);
				this.$contrastSlider.slider("value",state.contrast);
				
				this.disableUpdates = false;
			},
			
			valuesUpdated : function() {
				if (this.disableUpdates) return;
				
				this.viewer.state.brightness = this.$brightness.val();
				this.viewer.state.contrast = this.$contrast.val();
				
				this.updateUI();
				
				this.considerProcess();
			},
			
			considerProcess : function() {
				if (this.t) {
					clearInterval(this.t);
				}
				
				var self = this;
				this.t = setTimeout(function() {
					self.viewer.doProcess();
				},200)
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
				
				this.updateUI(state);
				
				return "process";
			},
			
			draw : function(g) {
			}
		});
		
		return This;
	}
)
