new function($) {
	
	$(document).ready(function() {
		$("#button").click(function() {
			$.post("@{ImageBrowser.fetch()}",{path:$("#pathField").val()},function(files) {
				$.each(files,function() {
					$("#browser").append("file:"+this.path+"<br/>");
				});
			},'json');
		});
	});
	
	
	
	
}(jQuery);
