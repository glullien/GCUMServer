function displayError(error) {
	$("#error").html("Error: " + error)
}

var district = 'All';
var latest = null;
function fillList() {
	var params;
	if (latest == null) params = {'answerCharset': 'UTF-8', 'number': 20, 'district': district};
	else params = {'answerCharset': 'UTF-8', 'number': 20, 'district': district, after: latest};
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
					if (photo.locationSource == "Device") html += '<span class="photoCoordinates">' + (photo.latitude / 1E5) + ' °N/' + (photo.longitude / 1E5) + ' °E</span>';
					if (photo.username != null) html += '<span class="username">' + photo.username + '</span>';
					html += '<a href="#" id="like' + photo.id + '" class="like' + (photo.isLiked ? ' isLiked' : '') + '" onclick="toggleLike(\'' + photo.id + '\');return false;">' + photo.likesCount + ' <i class="glyphicon glyphicon-heart"></i></a>';
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
		data: {'answerCharset': 'UTF-8', 'photoId': photoId},
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

function configureDistrictButton(d) {
	$("#district" + d).click(function () {
		var text;
		if (d == 1) text = "1er";
		else text = d + "e";
		setDistrict(d, text);
	})
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
	$("#filterOpen").click(function () {
		$("#filterDistrict").val(district);
		$("#filter").show();
	});
	$("#filterApply").click(function () {
		$("#filter").hide();
		var filterDistrict = $("#filterDistrict");
		setDistrict(filterDistrict.val(), filterDistrict.find("option:selected").text())
	})
});
