define(
	['view/RenderedView','text!tmpl/MoveTool.html'],
	function (RenderedView, tmpl) {
		var This = function() {
		};
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
		
			activate : function(state) {
				this.render();
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
				
				return true;
			},
			
			draw : function(g) {
				
			}
		});
		
		return This;
	}
)
