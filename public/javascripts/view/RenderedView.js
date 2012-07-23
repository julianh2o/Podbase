define(
	['view/View'],
	function (View) {
		return View.extend({
			initialize: function(){
				this.render();
			},
							
			render: function(){
				var html = this.template(this.model)
				
				$(this.el).html( html );
			}
		});
	}
)