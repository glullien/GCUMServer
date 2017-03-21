function displayError(error) {
	$("#error").html(error);
	$("#errorModal").modal("show");
}

var district = 'All';
var author = '<all>';
var sort = 'date';
var latest = null;
var currentPosition = null;

function convertRad(input) {
	return (Math.PI * input) / 180;
}

function distance(lat_a_degre, lon_a_degre, lat_b_degre, lon_b_degre) {
	var R = 6378000;
	var lat_a = convertRad(lat_a_degre);
	var lon_a = convertRad(lon_a_degre);
	var lat_b = convertRad(lat_b_degre);
	var lon_b = convertRad(lon_b_degre);
	return R * (Math.PI / 2 - Math.asin(Math.sin(lat_b) * Math.sin(lat_a) + Math.cos(lon_b - lon_a) * Math.cos(lat_b) * Math.cos(lat_a)))
}

function fillList() {
	var params = stdParams({number: 20, district: district, author: author, sort: sort});
	if (currentPosition != null) params = concatMaps(params, {
		latitude: Math.round(currentPosition.latitude * 1E5),
		longitude: Math.round(currentPosition.longitude * 1E5)
	});
	if (latest != null) params = concatMaps(params, {after: latest});
	$.ajax({
		url: 'getList',
		type: 'POST',
		data: params,
		dataType: 'json',
		success: function (json) {
			if (json.result == 'success') {
				var html = "";
				var controlsWidth = $("#controls").width();
				var maxSize = Math.min(400, controlsWidth - 30);
				for (var i = 0; i < json.photos.length; i++) {
					var photo = json.photos[i];
					var target = targetSize(photo.width, photo.height, 330);
					html += '<div class="frame photoAndLegend">';
					html += '<a href="#" onclick="openPhoto(\'' + photo.id + '\');return false;" class ="photoThumbnail">';
					html += '<img width="' + target.width + '" height="' + target.height + '" src="getPhoto?id=' + photo.id + '&maxSize=' + maxSize + '">';
					html += '</a>';
					var dateTime = photo.date;
					if (photo.time != "unknown") dateTime += " " + photo.time;
					html += '<span class="photoDate">' + dateTime + '</span>';
					var address = photo.street + ' dans le ' + photo.district + "e";
					if (photo.number != "unknown") address = photo.number + ", " + address;
					html += '<span class="photoAddress">' + address + '</span>';
					if (photo.locationSource == "Device") {
						if (currentPosition != null) {
							var d = distance(photo.latitude * (1.0 / 1E5), photo.longitude * (1.0 / 1E5), currentPosition.latitude, currentPosition.longitude);
							html += '<span class="photoDistance">' + Math.round(d) + ' m</span>';
						}
						html += '<span class="photoCoordinates">';
						html += (photo.latitude / 1E5) + ' °N/' + (photo.longitude / 1E5) + ' °E';
						html += '</span>';
					}
					if (photo.username != null) html += '<span class="username">' + photo.username + '</span>';
					html += '<a href="#" id="like' + photo.id + '" class="like' + (photo.isLiked ? ' isLiked' : '') + '" onclick="toggleLike(\'' + photo.id + '\');return false;">';
					html += photo.likesCount + ' <i class="glyphicon glyphicon-heart"></i>';
					html += '</a>';
					html += '</div>';
					latest = photo.id;
				}
				$("#list").append(html);
				$("#nbAfter").html("" + json.nbAfter);
			}
			else {
				displayError(json.message)
			}
		},
		error: function () {
			displayError("Cannot connect to server")
		}
	})
}

function toggleLike(photoId) {
	$.ajax({
		url: 'toggleLike',
		type: 'POST',
		data: stdParams({photoId: photoId}),
		dataType: 'json',
		success: function (json) {
			if (json.result == 'success') {
				var like = $("#like" + photoId);
				like.html(json.likesCount + ' <i class="glyphicon glyphicon-heart">');
				if (json.isLiked) like.addClass("isLiked");
				else like.removeClass("isLiked");
			}
		},
		error: function () {
		}
	});
}

function setDistrict(d, text) {
	$("#district").html(text);
	district = d;
	latest = null;
	$("#list").html("");
	fillList();
}

function setAuthor(d, text) {
	$("#authors").html(text);
	author = d;
	latest = null;
	$("#list").html("");
	fillList();
}

function configureDistrictButton(d) {
	$("#district" + d).click(function () {
		var text;
		if (d == 1) text = "1er";
		else text = d + "e";
		setDistrict(d, text);
	})
}

function geoSuccess(position) {
	$("#requestingPositionModal").modal("hide");
	currentPosition = position.coords;
	setSort("closest", "proximité");
}

function geoError(error) {
	$("#requestingPositionModal").modal("hide");
	displayError("Erreur (" + error.code + ") : " + error.message);
}

function setSort(s, text) {
	if ((s == "closest") && (currentPosition == null)) {
		if (navigator.geolocation) {
			$("#requestingPositionModal").modal("show");
			navigator.geolocation.getCurrentPosition(geoSuccess, geoError);
		}
		else {
			displayError("Votre navigateur ne fournit pas de service de localisation");
		}
		return;
	}
	$("#sort").html(text);
	sort = s;
	latest = null;
	$("#list").html("");
	fillList();
}

$(function () {
	fillList();
	$("#photoClose").click(function () {
		$("#photo").hide();
	});
	$("#filterClose").click(function () {
		$("#filter").hide();
	});
	$("#more").click(function () {
		fillList();
	});
	$("#districtAll").click(function () {
		setDistrict("All", "Tous");
	});
	for (var i = 1; i <= 20; i++) configureDistrictButton(i);
	$("#authorsAll").click(function () {
		setAuthor("<all>", "Tous");
	});
	$("#authorsMyself").click(function () {
		setAuthor("<myself>", "Moi-même");
	});
	$("#authorsText").keyup(function (e) {
		if (e.keyCode == 13) {
			var author = $('#authorsText').val();
			if (author.length > 0) {
				$("#authorsMenu").dropdown('toggle');
				setAuthor(author, author);
			}
			return false;
		}
	});
	$("#sortDate").click(function () {
		setSort("date", "date");
	});
	$("#sortClosest").click(function () {
		setSort("closest", "proximité");
	});
	$("#filterOpen").click(function () {
		$("#filterDistrict").val(district);
		$("#filterAuthor").val(author);
		$("#filterSort").val(sort);
		$("#filter").show();
	});
	$("#filterApply").click(function () {
		$("#filter").hide();
		var filterDistrict = $("#filterDistrict");
		var filterAuthor = $("#filterAuthor");
		var filterSort = $("#filterSort");
		setDistrict(filterDistrict.val(), filterDistrict.find("option:selected").text());
		setAuthor(filterAuthor.val(), filterAuthor.find("option:selected").text());
		setSort(filterSort.val(), filterSort.find("option:selected").text())
	});
	$(document).keyup(function (e) {
		if (e.keyCode === 27) {
			$('#photoClose').click();
		}
	});
});
