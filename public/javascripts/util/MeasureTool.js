define(
	['view/RenderedView','text!tmpl/MeasureTool.html'],
	function (RenderedView, tmpl) {
		var This = function($el) {
			this.calibrated = false;
			this.calibration = null;
			this.measure = null;
			this.calibrationLength = 1;
		};
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
		
			activate : function(viewer) {
				this.viewer = viewer;
				
				this.render();
				
				this.$legendLength = $(".legend-length",this.el);
				
				this.setCalibrationLength(1);
				
				this.$legendLength.change($.proxy(this.fieldUpdated,this));
			},
			
			update : function() {
				
			},
			
			fieldUpdated : function(e) {
				var num = parseFloat(this.$legendLength.val());
				this.setCalibrationLength(num);
			},
			
			setCalibrationLength : function(newLength) {
				var valueUpdated = false;
				if (newLength != this.calibrationLength) valueUpdated = true;
					
				this.calibrationLength = newLength;
				this.$legendLength.val(this.calibrationLength.toFixed(2));
				
				if (valueUpdated) this.viewer.updateCanvas();
			},
			
			deactivate : function() {
				this.el.empty();
			},
			
			mousedown : function(e,state) {
				if (e.shiftKey) {
					this.calibrated = false;
				}
			},
			
			mouseup : function(e,state) {
				this.calibrated = true;
			},
			
			mousemove : function(e,state) {
				
			},
			
			mousedrag : function(e,pos,origin,state,originState) {
				if (this.calibrated) {
					this.measure = {};
					this.measure.start = origin.relativeToBox(state.pan,state.effectiveDim());
					this.measure.end = pos.relativeToBox(state.pan,state.effectiveDim());
				} else {
					this.calibration = {};
					this.calibration.start = origin.relativeToBox(state.pan,state.effectiveDim());
					this.calibration.end = pos.relativeToBox(state.pan,state.effectiveDim());
				}
				
				return true;
			},
			
			draw : function(g,state) {
				if (this.calibration) {
					var start = this.calibration.start.boxToAbsolute(state.pan,state.effectiveDim());
					var end = this.calibration.end.boxToAbsolute(state.pan,state.effectiveDim());
					
					g.strokeStyle = '#f00';
					g.lineWidth   = 2;
					
					g.beginPath();
					g.moveTo(start.x,start.y);
					g.lineTo(end.x,end.y);
					g.stroke();
				}
				
				if (this.measure) {
					var start = this.measure.start.boxToAbsolute(state.pan,state.effectiveDim());
					var end = this.measure.end.boxToAbsolute(state.pan,state.effectiveDim());
					
					g.strokeStyle = '#00f';
					g.lineWidth   = 2;
					
					g.beginPath();
					g.moveTo(start.x,start.y);
					g.lineTo(end.x,end.y);
					g.stroke();
					
					var length = this.calibration.start.dist(this.calibration.end);
					var lpx = length / this.calibrationLength;
					
					var dist = this.measure.start.dist(this.measure.end);
					var unitsDist = (dist / lpx).toFixed(1);
					
					g.font="20px Arial";
					g.fillStyle = "#00f";
					g.fillText(unitsDist,end.x + 30, end.y + 30);
				}
			}
		});
		
		return This;
	}
)
