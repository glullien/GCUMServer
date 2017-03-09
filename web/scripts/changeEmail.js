$(function () {
	var emailRegex = /^.{1,40}@.{1,40}\..{1,20}$/;
	var emailField = $('#email');
	var emailGroup = $('#emailGroup');
	var submit = $('#submit');
	var remove = $('#remove');
	var status = $("#status");
	emailField.on("input", function () {
		var email = emailField.val();
		if ((email == "") || emailRegex.test(email)) emailGroup.removeClass("has-error");
		else emailGroup.addClass("has-error");
	});

	submit.click(function () {
		var email = emailField.val();
		if ((email != "") && !emailRegex.test(email)) emailGroup.addClass("has-error");
		if ((email != "") && !emailRegex.test(email)) status.html("L'email est incorrect");
		else {
			submit.prop("disabled", true);
			remove.prop("disabled", true);
			status.html("Connexion...");
			$.ajax({
				url: 'changeEmail',
				type: 'POST',
				data: {'answerCharset': 'UTF-8', 'email': email},
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') {
						$("#successModal").modal("show");
					}
					else status.html(json.message);
					submit.prop("disabled", false);
					remove.prop("disabled", false);
				},
				error: function () {
					status.html("Internal error");
					submit.prop("disabled", false);
					remove.prop("disabled", false);
				}
			});
		}
	});
	remove.click(function () {
		submit.prop("disabled", true);
		remove.prop("disabled", true);
		status.html("Connexion...");
		$.ajax({
			url: 'removeEmail',
			type: 'POST',
			data: {'answerCharset': 'UTF-8'},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') {
					$("#removeSuccessModal").modal("show");
				}
				else status.html(json.message);
				submit.prop("disabled", false);
				remove.prop("disabled", false);
			},
			error: function () {
				status.html("Internal error");
				submit.prop("disabled", false);
				remove.prop("disabled", false);
			}
		});
	});
});
