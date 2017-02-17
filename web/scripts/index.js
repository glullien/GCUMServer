function displayError(error) {
	$("#error").html("Error: " + error)
}
var map;
var infoWindow;
var markerCluster;
var contentString = '<div id="infoPhoto">' +
	'<p><a href="#" onclick="viewPhotos();return;">Nb photos: <span id="infoPhotoCount"/></a></p>' +
	'<p>Dates: <span id="infoPhotoDates"/></p>' +
	'<p>Street: <span id="infoPhotoStreet"/></p>' +
	'<p><img id="infoPhotoThumbnail" src=""></p>' +
	'</div>';
var currentPhotosIds;
function getPhotoView(photo) {
	var content = '<div class="photoAndLegend">';
	content += '<img src="getPhoto?id=' + photo.id + '&maxSize=400" class="photo">';
	var dateTime = photo.date;
	if (photo.time != "unknown") dateTime += " " + photo.time;
	content += '<span class="photoDate">' + dateTime + '</span>';
	if (photo.locationSource == "Device") content += '<span class="photoCoordinates">' + (photo.latitude / 1E5) + ' °N/' + (photo.longitude / 1E5) + ' °E</span>';
	if (photo.username != null) content += '<span class="username">' + photo.username + '</span>';
	content += '<a href="#" id="like' + photo.id + '" class="like' + (photo.isLiked ? ' isLiked' : '') + '" onclick="toggleLike(' + photo.id + ');return false;">' + photo.likesCount + ' <i class="glyphicon glyphicon-user"></i></a>';
	content += '</div>';
	return content;
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

var contentPhotos=null;
function viewPhotos() {
	if (contentPhotos == null) {
		for (var i = 0; i < currentPhotosIds.length; i++) contentPhotos += getPhotoView(currentPhotosIds[i]);
		$("#photosList").html(contentPhotos);
	}
	$("#photos").show();
}

function createMarker(latitude, longitude) {
	var marker = new google.maps.Marker({
		position: {lat: latitude * 1.0 / 1E5, lng: longitude * 1.0 / 1E5}
	});
	marker.addListener('click', function () {

		infoWindow.open(map, marker);
		$("#infoPhotoCount").html("...");
		$("#infoPhotoDates").html("...");
		$("#infoPhotoStreet").html("...");
		$("#infoPhotoThumbnail").attr("src", "/settings-512.png");
		$.ajax({
			url: 'getPointInfo',
			type: 'POST',
			data: {'latitude': latitude, 'longitude': longitude, 'timeFrame': timeFrame, 'locationSources': locationSources},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') {
					contentPhotos = null;
					currentPhotosIds = json.photos;
					$("#infoPhotoCount").html(json.photos.length);
					$("#infoPhotoDates").html(json.dates);
					$("#infoPhotoStreet").html(json.street);
					$("#infoPhotoThumbnail").attr("src", "getPhoto?id=" + json.photos[0].id + "&maxSize=150")
				}
				else {
					$("#infoPhotoCount").html(json.message)
				}
			},
			error: function () {
				$("#infoPhotoDate").html("Cannot connect to server")
			}
		});
	});
	return marker;
}

var timeFrame = 'All';
function changeTimeFrame(text, serverArg) {
	$("#date").html(text);
	timeFrame = serverArg;
	refreshMarkers();
}

var locationSources = 'Street,Device';
function changeLocationSource(text, serverArg) {
	$("#locationSource").html(text);
	locationSources = serverArg;
	refreshMarkers();
}

function refreshMarkers() {
	markerCluster.clearMarkers();
	$.ajax({
		url: 'getPoints',
		type: 'POST',
		data: {'zone': 'All', 'timeFrame': timeFrame, 'locationSources': locationSources},
		dataType: 'json',
		success: function (json) {
			if (json.result == 'success') {
				var markers = [];
				for (var i = 0; i < json.photos.length; i++) {
					var photo = json.photos[i];
					markers.push(createMarker(photo.latitude, photo.longitude));
				}
				markerCluster.addMarkers(markers);
			}
			else {
				displayError(json.message)
			}
		},
		error: function () {
			displayError("Cannot connect to server")
		}
	});
}

function initMap() {
	$("#photos").hide();
	$("#photosClose").click(function () {
		$("#photos").hide();
	});
	$(document).keyup(function (e) {
		if (e.keyCode === 27) $('#photosClose').click();
	});
	$("#dateAll").click(function () {
		changeTimeFrame("Tout", "All");
	});
	$("#dateLastDay").click(function () {
		changeTimeFrame("Dernier jour", "LastDay");
	});
	$("#dateLastWeek").click(function () {
		changeTimeFrame("Dernière semaine", "LastWeek");
	});
	$("#dateLastMonth").click(function () {
		changeTimeFrame("Dernier mois", "LastMonth");
	});
	$("#locationSourceAll").click(function () {
		changeLocationSource("Tout", "Street,Device");
	});
	$("#locationSourceGPS").click(function () {
		changeLocationSource("Par GPS", "Device");
	});
	map = new google.maps.Map(document.getElementById('map'), {
		center: {lat: 48.858607, lng: 2.345113},
		scrollwheel: false,
		zoom: 13
	});
	infoWindow = new google.maps.InfoWindow({
		content: contentString
	});
	markerCluster = new MarkerClusterer(map, null, {
		maxZoom: 16,
		imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'
	});
	refreshMarkers();
}
