function getCookieVal(cookie, offset) {
	var semiColonIndex = cookie.indexOf(";", offset);
	if (semiColonIndex == -1) semiColonIndex = cookie.length;
	return unescape(cookie.substring(offset, semiColonIndex));
}
function getCookie(name) {
	var arg = name + "=";
	var argLength = arg.length;
	var cookie = document.cookie;
	var cookieLength = cookie.length;
	var i = 0;
	while (i < cookieLength) {
		var j = i + argLength;
		if (cookie.substring(i, j) == arg) return getCookieVal(cookie, j);
		i = cookie.indexOf(" ", i) + 1;
		if (i == 0) break;
	}
	return null;
}

$(function () {
	$("#disconnect").click(function () {
		$.ajax({
			url: 'logout',
			type: 'POST',
			data: {},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') {
					document.cookie = "autoLogin=; expires=Sun, 28 Feb 2020 00:00:00 UTC; path=/";
					document.location.reload();
					document.location = "index.jsp";
				}
			},
			error: function () {
			}
		});
	});
});

function autoLogin() {
	var autoLoginCookie = getCookie("autoLogin");
	if (autoLoginCookie != null) {
		$.ajax({
			url: 'autoLogin',
			type: 'POST',
			data: {'cookie': autoLoginCookie},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') document.location.reload();
			},
			error: function () {
			}
		});
	}
}