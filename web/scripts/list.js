function displayError(error) {
	$("#error").html("Error: " + error)
}

var district = 'All';
var next = "Latest";
function fillList() {
	$.ajax({
		url: 'getList',
		type: 'POST',
		data: {'number': 20, 'district': district, 'start': next},
		dataType: 'json',
		success: function (json) {
			if (json.result == 'success') {
				var html = "";
				for (var i = 0; i < json.photos.length; i++) {
					var photo = json.photos[i];
					html += '<div class="frame photoAndLegend">';
					html += '<img src="getPhoto?id=' + photo.id + '&maxSize=400" class="photo">';
					var dateTime = photo.date;
					if (photo.time != "unknown") dateTime += " " + photo.time;
					html += '<span class="photoDate">' + dateTime + '</span>';
					html += '<span class="photoAddress">' + photo.street + ' dans le ' + photo.district + 'e</span>';
					if (photo.locationSource == "Device") html += '<span class="photoCoordinates">' + (photo.latitude / 1E5) + ' °N/' + (photo.longitude / 1E5) + ' °E</span>';
					if (photo.username != null) html += '<span class="username">' + photo.username + '</span>';
					html += '<a href="#" id="like' + photo.id + '" class="like' + (photo.isLiked ? ' isLiked' : '') + '" onclick="toggleLike(' + photo.id + ');return false;">' + photo.likesCount + ' <i class="glyphicon glyphicon-heart"></i></a>';
					html += '</div>';
					next = photo.id;
				}
				$("#list").append(html);
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

function setDistrict(d, text) {
	$("#district").html(text);
	district = d;
	next = "Latest";
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
	$("#more").click(function () {
		fillList();
	});
	$("#districtAll").click(function () {
		setDistrict("All", "Tous");
	});
	for (var i = 1; i <= 20; i++) configureDistrictButton(i);
});
