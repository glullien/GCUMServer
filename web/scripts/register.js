$(function () {
	var usernameField = $('#username');
	var usernameGroup = $('#usernameGroup');
	var usernameRegex = /^[a-zA-Z\d_]{1,20}$/;
	var passwordField = $('#password');
	var passwordGroup = $('#passwordGroup');
	var passwordRegex = /^.{6,20}$/;
	var passwordCheckField = $('#passwordCheck');
	var passwordCheckGroup = $('#passwordCheckGroup');
	var emailRegex = /^.{1,40}@.{1,40}\..{1,20}$/;
	var emailField = $('#email');
	var emailGroup = $('#emailGroup');
	var submit = $('#submit');
	var status = $("#status");
	usernameField.on("input", function () {
		var username = usernameField.val();
		if (usernameRegex.test(username)) usernameGroup.removeClass("has-error");
		else usernameGroup.addClass("has-error");
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
	emailField.on("input", function () {
		var email = emailField.val();
		if ((email == "") || emailRegex.test(email)) emailGroup.removeClass("has-error");
		else emailGroup.addClass("has-error");
	});

	submit.click(function () {
		var username = usernameField.val();
		var password = passwordField.val();
		var passwordCheck = passwordCheckField.val();
		var email = emailField.val();
		if (!usernameRegex.test(username)) usernameGroup.addClass("has-error");
		if (!passwordRegex.test(password)) passwordGroup.addClass("has-error");
		if (password != passwordCheck) passwordCheckGroup.addClass("has-error");
		if ((email != "") && !emailRegex.test(email)) emailGroup.addClass("has-error");
		if (!usernameRegex.test(username)) status.html("Le pseudo doit être constitué de 1 à 20 caractères alphanumériques ou de _");
		else if (!passwordRegex.test(password)) status.html("Le mot de passe doit être constitué de 6 à 20 caractères");
		else if (password != passwordCheck) status.html("Le mot de passe doit être retapé une deuxième fois");
		else if ((email != "") && !emailRegex.test(email)) status.html("L'email est incorrect");
		else {
			submit.prop("disabled", true);
			status.html("Connexion...");
			$.ajax({
				url: 'register',
				type: 'POST',
				data: {'username': username, 'password': password, 'email': email},
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') $("#successModal").modal("show");
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

	$("success").click(function () {
		document.location = "index.jsp";
	})
});

