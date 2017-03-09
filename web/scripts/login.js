$(function () {
	var usernameField = $('#username');
	var usernameGroup = $('#usernameGroup');
	var usernameRegex = /^[a-zA-Z\d_]{1,20}$/;
	var passwordField = $('#password');
	var passwordGroup = $('#passwordGroup');
	var passwordRegex = /^.{6,20}$/;
	var remindMe = $("#remindMe");
	var status = $("#status");
	var submit = $('#submit');
	var sendID = $("#sendID");
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

	$(document).keypress(function(e) {
		if(e.which == 13) {
			submit.click();
		}
	});

	submit.click(function () {
		var username = usernameField.val();
		var password = passwordField.val();
		var remindMeChecked = remindMe.prop("checked");
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
				data: {'answerCharset': 'UTF-8', 'username': username, 'password': password, "remindMe": remindMeChecked},
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') {
						if (remindMeChecked) document.cookie = ("autoLogin=" + json.autoLogin+"; expires="+json.validTo+" 00:00:00 UTC; path=/");
						document.location = "index.jsp";
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
	$("#forgotID").click(function () {
		$("#email").val("");
		$("#forgotIDModal").modal("show");
	});
	sendID.click(function () {
		var email = $("#email").val();
		if (email != "") {
			sendID.prop("disabled", true);
			$.ajax({
				url: 'sendID',
				type: 'POST',
				data: {'answerCharset': 'UTF-8', 'email': email},
				dataType: 'json',
				success: function (json) {
					$("#forgotIDModal").modal("hide");
					sendID.prop("disabled", false);
					if (json.result == 'success') $("#sendSuccessModal").modal("show");
					else $("#sendFailureModal").modal("show");
				},
				error: function () {
					$("#forgotIDModal").modal("hide");
					status.html("Internal error");
					sendID.prop("disabled", false);
				}
			});
		}
	});

});

