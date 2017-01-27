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
function getPhotoView(photoId) {
	return '<div class="photoAndLegend">' +
		'<img src="getPhoto?id=' + photoId + '&maxSize=400" class="photo">' +
		'<br/><span class="photoDate" id="legendId' + photoId + '">-</span>' +
		'</div>';
}
function downloadLegend(photoId) {
	$.ajax({
		url: 'getPhotoInfo',
		type: 'POST',
		data: {'id': photoId},
		dataType: 'json',
		success: function (json) {
			console.debug("json=" + json);
			if (json.result == 'success') {
				var legend = json.date;
				if (json.time != "unknown") legend += " " + json.time;
				$("#legendId" + photoId).html(legend);
			}
			else {
				$("#legendId" + photoId).html("error " + json.message);
			}
		},
		error: function () {
			$("#legendId" + photoId).html("error");
		}
	});
}
function viewPhotos() {
	var content = "";
	for (var i = 0; i < currentPhotosIds.length; i++) content += getPhotoView(currentPhotosIds[i]);
	$("#photosList").html(content);
	$("#photos").show();
	for (var i = 0; i < currentPhotosIds.length; i++) downloadLegend(currentPhotosIds[i]);
}

function createMarker(latitude, longitude) {
	var marker = new google.maps.Marker({
		position: {lat: latitude * 1.0 / 1E10, lng: longitude * 1.0 / 1E10},
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
			data: {'latitude': latitude, 'longitude': longitude, 'timeFrame': timeFrame},
			dataType: 'json',
			success: function (json) {
				if (json.result == 'success') {
					currentPhotosIds = json.ids;
					$("#infoPhotoCount").html(json.ids.length);
					$("#infoPhotoDates").html(json.dates);
					$("#infoPhotoStreet").html(json.street)
					$("#infoPhotoThumbnail").attr("src", "getPhoto?id=" + json.ids[0] + "&maxSize=150")
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
	$("#display").html(text)
	$("#displayChoices").hide();
	timeFrame = serverArg;
	refreshMarkers();
}

function refreshMarkers() {
	markerCluster.clearMarkers();
	$.ajax({
		url: 'getPoints',
		type: 'POST',
		data: {'zone': 'All', 'timeFrame': timeFrame},
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
	$("#displayChoices").hide();
	$("#photosClose").click(function () {
		$("#photos").hide();
	});
	$(document).keyup(function (e) {
		if (e.keyCode === 27) $('#photosClose').click();
	});
	$("#display").click(function (e) {
		$("#displayChoices").show();
	});
	$("#displayAll").click(function (e) {
		changeTimeFrame("tous", "All");
	});
	$("#displayLastDay").click(function (e) {
		changeTimeFrame("dernier jour", "LastDay");
	});
	$("#displayLastWeek").click(function (e) {
		changeTimeFrame("dernière semaine", "LastWeek");
	});
	$("#displayLastMonth").click(function (e) {
		changeTimeFrame("dernier mois", "LastMonth");
	});
	map = new google.maps.Map(document.getElementById('map'), {
		center: {lat: 48.858607, lng: 2.345113},
		scrollwheel: false,
		zoom: 13
	});
	infoWindow = new google.maps.InfoWindow({
		content: contentString
	});
	markerCluster = new MarkerClusterer(map, null, {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
	refreshMarkers();
}
