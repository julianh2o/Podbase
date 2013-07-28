define(
	['view/RenderedView', 'util/Point2d', 'data/Link', 'util/MoveTool', 'util/MeasureTool', 'util/AdjustTool', 'text!tmpl/Viewer.html'],
	function (RenderedView, Point2d, Link, MoveTool, MeasureTool, AdjustTool, tmpl) {
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
				this.tools = [];
				
				this.state = function() {};
				$.extend(this.state,{
						imageDim: new Point2d(options.width, options.height),
						scale: 1,
						slice: 1,
						pan: new Point2d(0,0),
						brightness: 0,
						contrast: 0,
						effectiveDim : function() {
							return this.imageDim.mul(this.scale);
						}
				});
				
				var usePath = this.path;
				if (usePath.startsWith("/data")) {
					usePath = usePath.substring("/data".length);
				}
				Link.imageMetadata(usePath).asap($.proxy(this.infoLoaded,this));
			},
			
			infoLoaded : function(link) {
				this.imageInfo = link.getData();
				this.refresh();
			},
				
			refresh : function() {
				this.render();
				
				this.$url = $(".url-field",this.el);
				this.updateUrl();
				
				window.viewer = this;
				
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
				
				
				this.$viewport.on("mousedown",$.proxy(this.mousedown,this));
				this.$viewport.on("mouseup",$.proxy(this.mouseup,this));
				this.$viewport.on("mousemove",$.proxy(this.mousemove,this));
				
				this.$viewport.mousewheel($.proxy(this.mousewheel,this));
				
				$(window).resize($.proxy(this.doResize,this));
				$(window).keypress($.proxy(this.keyPress,this));
				
				this.updateImage();
				
				this.doResize();
				this.state.scale = Math.min(this.state.canvasDim.x / this.state.imageDim.x, this.state.canvasDim.y / this.state.imageDim.y);
				this.stateUpdated();
			},
			
			updateUrl : function() {
				var url = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ":" + window.location.port : "")
				url += this.path;
				
				map = {
						brightness: this.state.brightness,
						contrast: this.state.contrast
				}
				
				url += "?" + $.param(map);;
				
				this.$url.text(url);
				this.$url.attr("href",url);
			},
			
			copyImageToProcess : function() {
				var g = this.$processed.get(0).getContext("2d");
				g.drawImage(this.$original.get(0),0,0);
			},
			
			stateUpdated : function() {
				if (this.tool && this.tool.update) this.tool.update();
				
				$(".slice",this.el).toggle(this.imageInfo.slices > 1);
				if (this.imageInfo.slices > 1) {
					$(".current-slice",this.el).text(this.state.slice);
					$(".total-slices",this.el).text(this.imageInfo.slices);
				}
				
				this.updateUrl();
			},
			
			addTool : function(name,display,object) {
				this.tools.push(object);
				var $li = $("<li>").addClass(name).attr("data-tool-name",name).data("tool",object);
				var $a = $("<a href='#'>").text(display);
				$li.append($a);
				this.$tools.append($li);
				object.init(this);
			},
			
			updateImage : function() {
				var url = this.path;
				if (this.imageInfo.slices > 1) {
					url += "?slice="+this.state.slice;
				}
				
				var img = new Image();
				this.original = img;
				
				$(img).load($.proxy(this.imageLoaded,this));
				img.src = url;
				
				this.imageLoading = true;
			},
			
			imageLoaded : function() {
				this.state.imageDim = new Point2d(this.original.width, this.original.height);
				
				this.imageLoading = false;
				this.imageProcessing = true;
					
				this.doProcess();
			},
			
			doProcess : function() {
				var brightness = this.state.brightness;
				var contrast = Math.pow(2,this.state.contrast/30) - 1;
				try {
					Pixastic.process(this.original, "brightness", {brightness:brightness,contrast:contrast},$.proxy(this.imageProcessingComplete,this));
				} catch (err) {
					//Hack to fix some very strange firefox issues relating to component not being ready despite the image load event triggering
					setTimeout($.proxy(function() {
						Pixastic.process(this.original, "brightness", {brightness:brightness,contrast:contrast},$.proxy(this.imageProcessingComplete,this));
					},this),100);
				}
			},
			
			imageProcessingComplete : function(image) {
				this.imageProcessing = false;
				this.processedImage = image;
				this.updateCanvas();
			},
			
			updateCanvas : function() {
				if (!this.processedImage) return;
				
				var g = this.$viewport.get(0).getContext("2d");
				g.clearRect(0,0,this.state.canvasDim.x,this.state.canvasDim.y);
				
				var scaledImageDim = this.state.effectiveDim();
				
				//                 source offset, width height  --- destination offset, width height
				//drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight)
				
				g.drawImage(
					this.processedImage,
					0,
					0,
					this.processedImage.width,this.processedImage.height,
					this.state.pan.x,this.state.pan.y,
					scaledImageDim.x,scaledImageDim.y
				);
				
				var state = this.state;
				_.each(this.tools,function(tool) {
					tool.draw(g,state);
				});
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
				
				this.stateUpdated();
				this.updateCanvas();
				
				event.preventDefault();
			},
			
			keyPress : function(e) {
				if (e.keyCode == 91 | e.keyCode == 93) {
					var previous = this.state.slice;
					this.state.slice = this.state.slice + (e.keyCode == 91 ? -1 : 1)
					this.state.slice = Math.max(1,Math.min(this.imageInfo.slices,this.state.slice));
					if (previous == this.state.slice) return;
					
					this.stateUpdated();
					this.updateImage();
				}
			},
			
			handleToolClick : function(e) {
				var $el = $(e.target).closest("li");
				
				var tool = $el.data("tool-name");
				this.selectTool(tool);
				
				e.preventDefault();
			},
			
			selectTool : function(tool) {
				var $el = this.$tools.find("[data-tool-name='"+tool+"']");
				if (this.tool) {
					this.tool.deactivate();
					$(this.tool.el).detach();
				}
				
				this.tool = $el.data("tool");
				
				$(".tool-options",this.el).append(this.tool.el);
				this.tool.activate(this);
				$el.siblings().removeClass("active");
				$el.addClass("active");
			},
			
			doResize : function() {
				var remaining = $(window).height() - this.$toolbar.height() - 50
				var width = $(window).width()-10;
				
				this.$viewport.width(width);
				this.$viewport.height(remaining);
				
				this.$viewport.attr("width",width);
				this.$viewport.attr("height",remaining);
				
				this.state.canvasDim = new Point2d(width,remaining);
				
				this.updateCanvas();
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)
