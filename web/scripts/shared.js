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
			data: {'answerCharset': 'UTF-8'},
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
	if ((autoLoginCookie != null) && (autoLoginCookie != "")) {
		$.ajax({
			url: 'autoLogin',
			type: 'POST',
			data: {'answerCharset': 'UTF-8', 'cookie': autoLoginCookie},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') document.location.reload();
			},
			error: function () {
			}
		});
	}
}

function targetSize(sourceWidth, sourceHeight, maxSize) {
	var targetWidth = 0;
	var targetHeight = 0;
	if ((sourceWidth < maxSize) && (sourceHeight < maxSize)) {
		targetWidth = sourceWidth;
		targetHeight = sourceHeight;
	}
	else {
		var ratio = sourceWidth * 1.0 / sourceHeight;
		if (ratio < 1) {
			targetWidth = Math.round(maxSize * ratio);
			targetHeight = maxSize;
		}
		else {
			targetWidth = maxSize;
			targetHeight = Math.round(maxSize / ratio);
		}
	}
	return {
		width: targetWidth,
		height: targetHeight
	};
}

function addAll(m, a) {
	for (var key in a) {
		if (a.hasOwnProperty(key)) {
			m[key] = a[key];
		}
	}
}
function concatMaps(m1, m2) {
	var res = {};
	addAll(res, m1);
	addAll(res, m2);
	return res;
}

function stdParams(m) {
	return concatMaps({answerCharset: 'UTF-8', device: 'web'}, m);
}