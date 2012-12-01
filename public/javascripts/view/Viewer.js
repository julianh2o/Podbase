define(
	['view/RenderedView', 'text!tmpl/Viewer.html'],
	function (RenderedView, tmpl) {
		function scaleRange(n,start,end,newStart,newEnd) {
			console.log(n,start,end,newStart,newEnd);
			var ratio = (n - start) / (end - start);
			return newStart + ratio * (newEnd - newStart)
		}
		
		function mousePosition(e) {
			var $el = $(e.target);
			return {x:e.pageX - $el.offset().left, y:e.pageY - $el.offset().top};
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.path = options.path;
				this.imageWidth = options.width;
				this.imageHeight = options.height;
				this.activeTool = null;
				
				this.state = {
						scale: 1,
						pan: {x:0,y:0},
						brightness: 0,
						contrast: 1,
						tools: {}
				};
				
				this.render();
				
				this.$tools = $(".tools",this.el)
				this.$tools.find("a").click($.proxy(this.handleToolClick,this));
				this.selectTool("measure");
				
				this.$image = $("img",this.el).hide();
				this.$viewport = $(".viewport",this.el);
				this.$toolbar = $(".toolbar",this.el);
				
				this.$image.load($.proxy(this.imageLoaded,this));
				
				this.$viewport.on("mousedown",$.proxy(this.mousedown,this));
				this.$viewport.on("mouseup",$.proxy(this.mouseup,this));
				this.$viewport.on("mousemove",$.proxy(this.mousemove,this));
				
				this.$viewport.mousewheel($.proxy(this.mousewheel,this));
				
				$(window).resize($.proxy(this.doResize,this));
				
				this.updateImage();
			},
			
			updateImage : function() {
				var url = this.path;
				url += "?scale=1";
				url += "&brightness="+this.state.brightness;
				url += "&contrast="+this.state.contrast;
				
				this.$image.attr("src",url);
				
				this.imageLoading = true;
			},
			
			imageLoaded : function() {
				this.imageWidth = this.$image.width();
				this.imageHeight = this.$image.height();
				
				this.imageLoading = false;
				
				this.updateCanvas();
			},
			
			updateCanvas : function() {
				var g = this.$viewport.get(0).getContext("2d");
				
				var canvasWidth = this.$viewport.width();
				var canvasHeight = this.$viewport.height();
				
				g.clearRect(0,0,canvasWidth,canvasHeight);
				
				//                 source offset, width height  --- destination offset, width height
				//drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight)
				var imgWidth = this.$image.width();
				var imgHeight = this.$image.height();
				
				g.drawImage(
						this.$image.get(0),
						0,
						0,
						imgWidth,
						imgHeight,
						this.state.pan.x,
						this.state.pan.y,
						imgWidth*this.state.scale,
						imgHeight*this.state.scale);
				
				if (this.state.tools.measure) {
					var measure = this.state.tools.measure;
					
					if (measure.legend) {
						var start = this.imageRelToPoint(measure.legend.start.x,measure.legend.start.y);
						var end = this.imageRelToPoint(measure.legend.end.x,measure.legend.end.y);
						
						g.strokeStyle = '#f00';
						g.lineWidth   = 2;
						
						g.beginPath();
						g.moveTo(start.x,start.y);
						g.lineTo(end.x,end.y);
						g.stroke();
					}
					
					if (this.state.tools.measure.current) {
						var start = this.imageRelToPoint(measure.current.start.x,measure.current.start.y);
						var end = this.imageRelToPoint(measure.current.end.x,measure.current.end.y);
						
						g.strokeStyle = '#00f';
						g.lineWidth   = 2;
						
						g.beginPath();
						g.moveTo(start.x,start.y);
						g.lineTo(end.x,end.y);
						g.stroke();
						
						var legend = Math.sqrt( Math.pow(measure.legend.end.x - measure.legend.start.x,2) + Math.pow(measure.legend.end.y - measure.legend.start.y,2) );
						var lpx = legend / measure.units;
						
						var dist = Math.sqrt( Math.pow(measure.current.end.x - measure.current.start.x,2) + Math.pow(measure.current.end.y - measure.current.start.y,2) );
						var unitsDist = dist / lpx;
						
						g.font="10px Arial";
						g.fillText(unitsDist,end.x + 30, end.y + 30);
					}
				}
				
				
			},
			
			mousedown : function(e) {
				this.originState = $.extend({},this.state);
				this.origin = mousePosition(e); //{x:e.pageX,y:e.pageY};
				
				if (e.shiftKey && this.activeTool == "measure") {
					this.originState.tools.measure = null;
				}
				
				e.preventDefault();
			},
			
			mouseup : function(e) {
				this.origin = null;
				
				if (this.activeTool == "measure" && this.state.tools.measure.units == null) {
					var units = parseFloat(prompt("distance"));
					this.state.tools.measure.units = units;
				}
				
				e.preventDefault();
			},
			
			mousemove : function(e) {
				this.mouse = mousePosition(e); //{x:e.pageX,y:e.pageY};
				
				if (this.origin) {
					var delta = {x:this.mouse.x - this.origin.x,y:this.mouse.y - this.origin.y};
					
					if (this.activeTool == "move") {
						this.state.pan = {x: this.originState.pan.x + delta.x,y: this.originState.pan.y + delta.y};
						this.updateCanvas();
					} else if (this.activeTool == "adjust"){
						this.state.brightness = scaleRange(this.mouse.x,0,this.$viewport.width(),-255,255);
						this.state.contrast = scaleRange(this.mouse.y,0,this.$viewport.height(),0,10);
						if (!this.imageLoading) this.updateImage();
					} else if (this.activeTool == "measure") {
						var units = null;
						
						if (this.originState.tools.measure && this.originState.tools.measure.units) {
							units = this.originState.tools.measure.units;
							this.state.tools.measure.legend = this.originState.tools.measure.legend;
							this.state.tools.measure.current = {
								start:this.pointToImageRel(this.origin.x,this.origin.y),
								end:this.pointToImageRel(this.mouse.x,this.mouse.y),
							}
						} else {
							this.state.tools.measure = {};
							this.state.tools.measure.legend = {
									start:this.pointToImageRel(this.origin.x,this.origin.y),
									end:this.pointToImageRel(this.mouse.x,this.mouse.y),
									units:units
									};
						}
						
						this.updateCanvas();
					}
				}
			},
			
			pointToImageRel : function(x,y) {
				var imgRel = {x: x - this.state.pan.x, y: y - this.state.pan.y};
				
				var imgDim = {};
				imgDim.x = this.$image.width() * this.state.scale;
				imgDim.y = this.$image.height() * this.state.scale;
				
				var relRel = {x: imgRel.x / imgDim.x, y: imgRel.y / imgDim.y};
				
				return relRel;
			},
			
			imageRelToPoint : function(x,y) {
				var imgDim = {};
				imgDim.x = this.$image.width() * this.state.scale;
				imgDim.y = this.$image.height() * this.state.scale;
				
				var x = x * imgDim.x + this.state.pan.x
				var y = y * imgDim.y + this.state.pan.y
				
				return {x: x, y: y};
			},
			
			mousewheel : function(event, delta, deltaX, deltaY) {
				var irp = this.pointToImageRel(this.mouse.x,this.mouse.y);
				
				if (deltaY < 0) {
					this.state.scale *= .9;
				} else if (deltaY > 0) {
					this.state.scale /= .9;
				}
				
				var newPos = this.imageRelToPoint(irp.x, irp.y);
				
				this.state.pan.x -= newPos.x - this.mouse.x;
				this.state.pan.y -= newPos.y - this.mouse.y;
				
				this.updateCanvas();
			},
			
			handleToolClick : function(e) {
				var $el = $(e.target).closest("li");
				
				var tool = $el.data("tool");
				this.selectTool(tool);
				
				e.preventDefault();
			},
			
			selectTool : function(tool) {
				this.activeTool = tool;
				var $el = this.$tools.find("[data-tool='"+tool+"']");
				$el.siblings().removeClass("active");
				$el.addClass("active");
			},
			
			doResize : function() {
				var remaining = $(window).height() - this.$toolbar.height() - 30
				this.$viewport.height(remaining);
				this.$viewport.width(remaining);
				this.$viewport.attr("width",remaining);
				this.$viewport.attr("height",remaining);
				
				this.updateCanvas();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
