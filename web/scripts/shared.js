$(function () {
	$("#disconnect").click(function () {
		$.ajax({
			url: 'logout',
			type: 'POST',
			data: {},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') {
					document.location.reload();
					document.location = "index.jsp";
				}
			},
			error: function () {
			}
		});
	});
});
