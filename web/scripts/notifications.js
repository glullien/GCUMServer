$(function () {
	var likedEmail = $('#likedEmail');
	var newsEmail = $('#newsEmail');
	var submit = $('#submit');
	var status = $("#status");

	submit.click(function () {
		var isLikedEmail = likedEmail.prop('checked');
		var isNewsEmail = newsEmail.prop('checked');
		submit.prop("disabled", true);
		status.html("Connexion...");
		$.ajax({
			url: 'setNotifications',
			type: 'POST',
			data: {'answerCharset': 'UTF-8', 'isLikedEmail': isLikedEmail, 'isNewsEmail': isNewsEmail},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') {
					$("#successModal").modal("show");
				}
				else status.html(json.message);
				submit.prop("disabled", false);
			},
			error: function () {
				status.html("Internal error");
				submit.prop("disabled", false);
			}
		});
	});
});


