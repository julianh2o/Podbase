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
			
			init : function(viewer) {
				this.viewer = viewer;
				
				this.render();
				
				this.$brightnessSlider = $(".brightness-slider",this.el).slider({
					min: -150,
					max: 150,
					step: 1,
					slide: $.proxy(this.sliderUpdated,this,"brightness")
				});
				
				this.$contrastSlider = $(".contrast-slider",this.el).slider({
					min: -150,
					max: 150,
					step: 1,
					slide: $.proxy(this.sliderUpdated,this,"contrast")
				});
				
				this.$brightness = $(".brightness",this.el);
				this.$contrast = $(".contrast",this.el);
				
				this.$brightness.change($.proxy(this.valuesUpdated,this));
				this.$contrast.change($.proxy(this.valuesUpdated,this));
			},
		
			activate : function(viewer) {
				this.updateUI(viewer.state);
			},
			
			sliderUpdated : function(kind,e,ui) {
				if (this.disableUpdates) return;
				
				if (kind == "brightness") {
					this.viewer.state.brightness = ui.value;
				}
				
				if (kind == "contrast") {
					this.viewer.state.contrast = ui.value;
				}
				
				this.viewer.stateUpdated();
				
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
				
				this.viewer.stateUpdated();
				
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
				return false;
			},
			
			draw : function(g) {
			}
		});
		
		return This;
	}
)
