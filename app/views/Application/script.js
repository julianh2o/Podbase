

	
	
//	TabPanel = Backbone.View.extend({
//		initialize: function(){
//			this.render();
//		},
//		render: function(){
//			var template = _.template( $("#TabPanel-template").html(), {tabs:this.options.tabs} );
//			$(this.el).html(template);
//			$(this.el).children().first().tabs();
//		}
//	});
//	
//	Project = Backbone.Model.extend({
//	});
//	
//	ProjectList = Backbone.Collection.extend({
//		model: Project,
//		url: '@{Application.getProjects}'
//	});
//	
//	ProjectsTab = Backbone.View.extend({
//		initialize: function(){
//			_each(this.options,)
//			var template = _.template( $("#Projects-template").html(), {tabs:this.options.tabs} );
//			$(this.el).html(template);
//			$(this.el).children().first().tabs();
//			
//			this.render();
//		},
//		render: function(){
//			
//			console.log(this.options);
//		}
//	});
//	
//	
//	var projects = new ProjectsTab();
//	
//	var tabs = [
//	            {id:'projects',name:'Projects',content:projects.el},
//	            {id:'b',name:'B tab',content:'content b'},
//	            {id:'c',name:'C tab',content:'zoink b'},
//	           ];
//	var tab = new TabPanel({tabs:tabs});
//	$("body").append(tab.el);