function displayError(error) {
	$("#error").html("Error: " + error)
}

var district = 'All';
var latest = null;
function fillList() {
	var params;
	if (latest == null) params = {'number': 20, 'district': district};
	else params = {'number': 20, 'district': district, after: latest};
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
					html += '<span class="photoAddress">' + photo.street + ' dans le ' + photo.district + 'e</span>';
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

function openPhoto(id) {
	$("#photo").show();
	var photoImg = $("#photoImg");
	var photoPane = $("#photoPane");
	var width = photoPane.width();
	var height = photoPane.height();
	photoImg.attr('src', 'settings-512.png');
	photoImg.attr('src', 'getPhoto?id=' + id + '&maxWidth=' + width + '&maxHeight=' + height);
	$("#photoAuthor").html("...");
	$("#photoDate").html("...");
	$("#photoStreet").html("...");
	$("#photoDistrict").html("...");
	$("#photoLatitude").html("...");
	$("#photoLongitude").html("...");
	$("#photoSize").html("...");
	$("#photoLikes").html("...");
	var $photoDownload = $("#photoDownload");
	$photoDownload.attr("href", "getPhoto?id=" + id + "&original=true");
	$photoDownload.attr("download", "photo" + id + ".jpg");
	$("#photoView").attr("href", "getPhoto?id=" + id + "&original=true");
	$.ajax({
		url: 'getPhotoInfo',
		type: 'POST',
		data: {'id': id},
		dataType: 'json',
		success: function (json) {
			if (json.result == 'success') {
				var username = json.username;
				if (username == null) username = "";
				$("#photoAuthor").html(username);
				var dateTime = json.date;
				if (json.time != "unknown") dateTime += " à " + json.time;
				$("#photoDate").html(dateTime);
				$("#photoStreet").html(json.street);
				$("#photoDistrict").html("" + json.district + ((json.district == 1) ? "er" : "e"));
				$("#photoLatitude").html((json.latitude / 1E5) + ' °N');
				$("#photoLongitude").html((json.longitude / 1E5) + ' °E');
				$("#photoSize").html("" + json.width + " x " + json.height);
				var likes = "";
				for (var i = 0; i < json.likes.length; i++) {
					if (likes != "") likes += ", ";
					likes += json.likes[i];
				}
				console.log("res " + likes);
				$("#photoLikes").html(likes);
			} else {
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
		data: {'photoId': photoId},
		dataType: 'json',
		success: function (json) {
			if (json.result == 'success') {
				var like = $("#like" + photoId);
				like.html(json.likesCount + ' <i class="glyphicon glyphicon-user">');
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
