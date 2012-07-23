(function($) {
	$(document).ready(function() {
		$(".permission").click(function() {
			var call = #{jsAction @ProjectController.setUserPermission(':projectId',':userId',':permission',':value') /}
			var userId = $(this).parents(".user").attr("data-userId");
			var permission = $(this).attr("value");
			var checked = $(this).is(":checked");
			$.get(call({'projectId':projectId,'userId':userId,'permission':permission,'value':checked}));
		});
	});
}(jQuery));
