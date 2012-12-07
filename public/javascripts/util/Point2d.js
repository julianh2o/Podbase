define(
	[],
	function () {
		var This = function(x,y) {
			this.x = x;
			this.y = y;
		};
		
		$.extend(This.prototype,{
			plus : function(pt) {
				return new This(this.x + pt.x, this.y + pt.y);
			},
			
			minus : function(pt) {
				return new This(this.x - pt.x, this.y - pt.y);
			},
			
			neg : function() {
				return this.mul(-1);
			},
			
			dist : function(pt) {
				if (!pt) pt = new Point2d(0,0);
				return Math.sqrt(Math.pow(pt.x - this.x,2) + Math.pow(pt.y - this.y,2));
			},
			
			mul : function(n) {
				return new This(this.x * n, this.y * n);
			},
			
			componentMul : function(pt) {
				return new This(this.x * pt.x, this.y * pt.y);
			},
			
			reciprocal : function() {
				return new This(1/this.x, 1/this.y);
			},
			
			relativeToBox : function(origin,dim) {
				var imgRel = this.minus(origin);
				
				var relRel = imgRel.componentMul(dim.reciprocal());
				
				return relRel;
			},
			
			boxToAbsolute : function(origin,dim) {
				return this.componentMul(dim).plus(origin);
			},
			
			toString : function() {
				return "("+this.x+", "+this.y+")";
			}
		});
		
		return This;
	}
)
