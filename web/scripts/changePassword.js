$(function () {
	var oldPasswordField = $('#oldPassword');
	var oldPasswordGroup = $('#oldPasswordGroup');
	var passwordField = $('#password');
	var passwordGroup = $('#passwordGroup');
	var passwordRegex = /^.{6,20}$/;
	var passwordCheckField = $('#passwordCheck');
	var passwordCheckGroup = $('#passwordCheckGroup');
	var submit = $('#submit');
	var status = $("#status");
	oldPasswordField.on("input", function () {
		if (passwordRegex.test(oldPasswordField.val())) oldPasswordGroup.removeClass("has-error");
		else oldPasswordGroup.addClass("has-error");
	});
	passwordField.on("input", function () {
		var password = passwordField.val();
		var passwordCheck = passwordCheckField.val();
		if (passwordRegex.test(password)) passwordGroup.removeClass("has-error");
		else passwordGroup.addClass("has-error");
		if (password == passwordCheck) passwordCheckGroup.removeClass("has-error");
		else passwordCheckGroup.addClass("has-error");
	});
	passwordCheckField.on("input", function () {
		var password = passwordField.val();
		var passwordCheck = passwordCheckField.val();
		if (password == passwordCheck) passwordCheckGroup.removeClass("has-error");
		else passwordCheckGroup.addClass("has-error");
	});

	submit.click(function () {
		var oldPassword = oldPasswordField.val();
		var password = passwordField.val();
		var passwordCheck = passwordCheckField.val();
		if (!passwordRegex.test(oldPassword)) oldPasswordGroup.addClass("has-error");
		if (!passwordRegex.test(password)) passwordGroup.addClass("has-error");
		if (password != passwordCheck) passwordCheckGroup.addClass("has-error");
		if (!passwordRegex.test(oldPassword)) status.html("L'ancien mot de passe est incorrect");
		else if (!passwordRegex.test(password)) status.html("Le mot de passe doit être constitué de 6 à 20 caractères");
		else if (password != passwordCheck) status.html("Le mot de passe doit être retapé une deuxième fois");
		else {
			submit.prop("disabled", true);
			status.html("Connexion...");
			$.ajax({
				url: 'changePassword',
				type: 'POST',
				data: {'answerCharset': 'UTF-8', 'oldPassword': oldPassword, 'password': password},
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
		}
	});
});

