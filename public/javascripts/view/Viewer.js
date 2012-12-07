define(
	['view/RenderedView', 'util/Point2d', 'util/MoveTool', 'util/MeasureTool', 'util/AdjustTool', 'text!tmpl/Viewer.html'],
	function (RenderedView, Point2d, MoveTool, MeasureTool, AdjustTool, tmpl) {
		//TODO dedupe from AdjustTool.js
		function scaleRange(n,start,end,newStart,newEnd) {
			var ratio = (n - start) / (end - start);
			return newStart + ratio * (newEnd - newStart)
		}
		
		function mousePosition(e) {
			var $el = $(e.target);
			return new Point2d(e.pageX - $el.offset().left, e.pageY - $el.offset().top);
		}
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function(options) {
				this.path = options.path;
				this.tool = null;
				
				this.state = function() {};
				$.extend(this.state,{
						imageDim: new Point2d(options.width, options.height),
						scale: 1,
						pan: new Point2d(0,0),
						brightness: 0,
						contrast: 0,
						effectiveDim : function() {
							return this.imageDim.mul(this.scale);
						}
				});
				
				
				this.render();
				
				
				this.$tools = $(".tools",this.el)
				this.$toolOptions = $(".tool-options",this.el);
				
				this.addTool("move","Move",new MoveTool());
				this.addTool("measure","Measure",new MeasureTool());
				this.addTool("adjust","Adjust",new AdjustTool());
				
				this.$tools.find("a").click($.proxy(this.handleToolClick,this));
				
				this.selectTool("move");
				
				this.$original = $(".original",this.el);
				this.$processed = $(".processed",this.el);
				
				this.$viewport = $(".viewport",this.el);
				this.$toolbar = $(".toolbar",this.el);
				
				this.$original.load($.proxy(this.imageLoaded,this));
				
				this.$viewport.on("mousedown",$.proxy(this.mousedown,this));
				this.$viewport.on("mouseup",$.proxy(this.mouseup,this));
				this.$viewport.on("mousemove",$.proxy(this.mousemove,this));
				
				this.$viewport.mousewheel($.proxy(this.mousewheel,this));
				
				$(window).resize($.proxy(this.doResize,this));
				
				this.updateImage();
			},
			
			copyImageToProcess : function() {
				var g = this.$processed.get(0).getContext("2d");
				g.drawImage(this.$original.get(0),0,0);
			},
			
			addTool : function(name,display,object) {
				var $li = $("<li>").addClass(name).attr("data-tool-name",name).data("tool",object);
				var $a = $("<a href='#'>").text(display);
				object.el = this.$toolOptions;
				$li.append($a);
				this.$tools.append($li);
			},
			
			updateImage : function() {
				var url = this.path;
				
				this.$original.attr("src",url);
				
				this.imageLoading = true;
			},
			
			imageLoaded : function() {
				this.state.imageDim = new Point2d(this.$original.width(), this.$original.height());
				this.$processed.attr({"width":this.state.imageDim.x,"height":this.state.imageDim.y});
				this.copyImageToProcess();
				
				this.imageLoading = false;
				this.imageProcessing = true;
				
				this.doProcess();
			},
			
			doProcess : function() {
				//this.state.brightness and contrast between -100 and 100
				var brightness = this.state.brightness;
				var contrast = this.state.contrast;
				Pixastic.process(this.$processed.get(0), "brightness", {brightness:brightness,contrast:contrast},$.proxy(this.imageProcessingComplete,this));
			},
			
			imageProcessingComplete : function(image) {
				this.imageProcessing = false;
				this.processedImage = image;
				this.updateCanvas();
			},
			
			updateCanvas : function() {
				var g = this.$viewport.get(0).getContext("2d");
				
				this.state.canvasDim = new Point2d(this.$viewport.width(), this.$viewport.height());
				
				g.clearRect(0,0,this.state.canvasDim.x,this.state.canvasDim.y);
				
				//                 source offset, width height  --- destination offset, width height
				//drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight)
				
				var image = this.$processed.get(0);
				if (this.processedImage) image = this.processedImage;
				
				var scaledImageDim = this.state.effectiveDim();
				g.drawImage(
						image,
						0,
						0,
						this.state.imageDim.x,
						this.state.imageDim.y,
						this.state.pan.x,
						this.state.pan.y,
						scaledImageDim.x,
						scaledImageDim.y);
				
				this.tool.draw(g,this.state);
			},
			
			mousedown : function(e) {
				this.mouse = mousePosition(e);
				
				this.origin = this.mouse;
				this.originState = $.extend({},this.state);
				
				this.tool.mousedown(e,this.origin,this.state);
				
				e.preventDefault();
			},
			
			mouseup : function(e) {
				this.mouse = mousePosition(e);
				
				this.tool.mouseup(e,this.mouse,this.state);
				
				this.origin = null;
				
				e.preventDefault();
			},
			
			mousemove : function(e) {
				this.mouse = mousePosition(e);
				
				var response = false;
				if (this.origin) {
					response = this.tool.mousedrag(e,this.mouse,this.origin,this.state,this.originState);
				} else {
					response = this.tool.mousemove(e,this.mouse,this.origin,this.state,this.originState);
				}
				
				if (response == "process") {
					this.doProcess();
				} else if (response) {
					this.updateCanvas();
					
				}
 				
				
//					var delta = {x:this.mouse.x - this.origin.x,y:this.mouse.y - this.origin.y};
					
//					if (this.activeTool == "adjust"){
//						this.state.brightness = scaleRange(this.mouse.x,0,this.$viewport.width(),-255,255);
//						this.state.contrast = scaleRange(this.mouse.y,0,this.$viewport.height(),0,10);
//						if (!this.imageLoading) this.updateImage();
//					}
			},
			
			mousewheel : function(event, delta, deltaX, deltaY) {
				var scaledImageDim = this.state.effectiveDim();
				
				var irp = this.mouse.relativeToBox(this.state.pan, scaledImageDim); 
				
				if (deltaY < 0) {
					this.state.scale *= .9;
				} else if (deltaY > 0) {
					this.state.scale /= .9;
				}
				
				scaledImageDim = this.state.effectiveDim();
				var newPos = irp.boxToAbsolute(this.state.pan,scaledImageDim)
				
				this.state.pan = this.state.pan.minus(newPos.minus(this.mouse));
				
				this.updateCanvas();
			},
			
			handleToolClick : function(e) {
				var $el = $(e.target).closest("li");
				
				var tool = $el.data("tool-name");
				this.selectTool(tool);
				
				e.preventDefault();
			},
			
			selectTool : function(tool) {
				var $el = this.$tools.find("[data-tool-name='"+tool+"']");
				if (this.tool) this.tool.deactivate();
				
				this.tool = $el.data("tool");
				
				this.tool.activate(this);
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
