$(function () {
	var usernameField = $('#username');
	var usernameGroup = $('#usernameGroup');
	var usernameRegex = /^[a-zA-Z\d_]{1,20}$/;
	var passwordField = $('#password');
	var passwordGroup = $('#passwordGroup');
	var passwordRegex = /^.{6,20}$/;
	var status = $("#status");
	var submit = $('#submit');
	var status = $("#status");
	usernameField.on("input", function () {
		var username = usernameField.val();
		if (usernameRegex.test(username)) usernameGroup.removeClass("has-error");
		else usernameGroup.addClass("has-error");
	});
	passwordField.on("input", function () {
		var password = passwordField.val();
		if (passwordRegex.test(password)) passwordGroup.removeClass("has-error");
		else passwordGroup.addClass("has-error");
	});

	submit.click(function () {
		var username = usernameField.val();
		var password = passwordField.val();
		if (!usernameRegex.test(username)) usernameGroup.addClass("has-error");
		if (!passwordRegex.test(password)) passwordGroup.addClass("has-error");
		if (!usernameRegex.test(username)) status.html("Le pseudo doit être constitué de 1 à 20 caractères alphanumériques ou de _");
		else if (!passwordRegex.test(password)) status.html("Le mot de passe doit être constitué de 6 à 20 caractères");
		else {
			submit.prop("disabled", true);
			status.html("Connexion...");
			$.ajax({
				url: 'login',
				type: 'POST',
				data: {'username': username, 'password': password},
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') document.location = "index.jsp";
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

