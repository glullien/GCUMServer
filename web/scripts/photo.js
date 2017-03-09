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

