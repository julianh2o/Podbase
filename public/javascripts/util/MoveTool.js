define(
	['view/RenderedView','text!tmpl/MoveTool.html'],
	function (RenderedView, tmpl) {
		var This = function() {
		};
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
		
			init : function(viewer) {
				this.viewer = viewer;
				this.render();
				
				this.$x = $(".x",this.el);
				this.$y = $(".y",this.el);
				this.$scale = $(".scale",this.el);
				
				this.update(viewer.state);
			},
			
			activate : function(viewer) {
				
			},
			
			update : function() {
				var state = this.viewer.state;
				this.$x.text(Math.round(state.pan.x));
				this.$y.text(Math.round(state.pan.y));
				var percent = Math.round(state.scale * 100);
				this.$scale.text(percent+"%");
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
				var delta = pos.plus(origin.neg());
				
				state.pan = originState.pan.plus(delta);
				
				this.viewer.stateUpdated();
				
				return true;
			},
			
			draw : function(g) {
				
			}
		});
		
		return This;
	}
)
