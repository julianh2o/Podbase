new function($) {
	function ImageViewer(src) {
		this.src = src;
		
		this.scale = 1;
		
		this.brightness = 0;
		this.contrast = 1;
		
		var that = this;
		this.render = function() {
			var url = src;
			url += "?scale="+that.scale;
			url += "&brightness="+that.brightness;
			url += "&contrast="+that.contrast;
			var img = $("#image");
			img.attr("src",url);
			img.attr("width",that.getWidth());
			img.attr("height",that.getHeight());
		}
		
		this.calculateScale = function(maxwidth, maxheight) {
			var width_ratio = width/maxwidth;
			var height_ratio = height/maxheight;
			if (width_ratio > height_ratio) {
				//resize using width as max
				this.scale = 1/width_ratio;
			} else {
				//resize using height as max
				this.scale = 1/height_ratio;
			}
		}
		
		this.getWidth = function() {
			return width*this.scale;
		}
		
		this.getHeight = function() {
			return height*this.scale;
		}
		
		this.zoomin = function() {
			that.scale *= 1.5;
			that.render();
		}
		
		this.zoomout = function() {
			that.scale /= 1.5;
			that.render();
		}
		
		this.brighten = function() {
			that.brightness += 10;
			that.render();
		}
		
		this.darken = function() {
			that.brightness -= 10;
			that.render();
		}
		
		this.decreaseContrast = function() {
			that.contrast += .1;
			that.render();
		}
		
		this.increaseContrast = function() {
			that.contrast -= .1;
			that.render();
		}
	}
	
	var view;
	$(document).ready(function() {
		view = new ImageViewer(path);
		view.calculateScale(900,600);
		view.render();
		
		$(".zoomin").click(view.zoomin);
		$(".zoomout").click(view.zoomout);
		
		$(".brighten").click(view.brighten);
		$(".darken").click(view.darken);
		
		$(".increaseContrast").click(view.increaseContrast);
		$(".decreaseContrast").click(view.decreaseContrast);
	});
}(jQuery);
