<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GCUM</title>
    <style>
        #map {
            height: 750px;
            width: 100%;
        }

        #photos {
            position: fixed;
            left: 80px;
            right: 80px;
            top: 80px;
            padding: 20px;
            border: 4px solid black;
            background: white;
        }

        #photosList {
            overflow: auto;
            height: 530px;
            display: inline-block;
        }

        .photo {
        }

        .photoAndLegend {
            margin-right: 10px;
            margin-top: 10px;
            display: inline-block;
        }
    </style>
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
</head>
<body>
<div id="map"></div>
<div id="photos">
    <p><a href="#" onclick="closePhotos();return false;">X</a></p>
    <div id="photosList"></div>
</div>
<p>
    <span id="error"></span>
</p>
<script>
	function displayError(error) {
		$("#error").html("Error: " + error)
	}
	var map;
	var infoWindow;
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
			'<br/><span id="legendId' + photoId + '">-</span>' +
			'</div>';
	}
	function downloadLegend(photoId) {
		$.ajax({
			url: 'getPhotoInfo',
			type: 'POST',
			data: {'id': photoId},
			dataType: 'json',
			success: function (json) {
				console.debug("json="+json);
				if (json.result == 'success') {
					console.debug("json.date="+json.date);
					var legend = json.date;
					console.debug("json.time="+json.time);
					if (json.time != "unknown") legend += " " + json.time;
					console.debug("legend="+legend);
					$("#legendId" + photoId).html(legend);
				}
				else {
					console.debug("json error");
					$("#legendId" + photoId).html("error " + json.message);
				}
			},
			error: function () {
				console.debug("sys error");
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
	function closePhotos() {
		$("#photos").hide();
	}
	function addMarker(latitude, longitude) {
		var marker = new google.maps.Marker({
			position: {lat: latitude * 1.0 / 1E10, lng: longitude * 1.0 / 1E10},
			map: map
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
				data: {'latitude': latitude, 'longitude': longitude},
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
	}
	function initMap() {
		closePhotos();
		// Create a map object and specify the DOM element for display.
		map = new google.maps.Map(document.getElementById('map'), {
			center: {lat: 48.858607, lng: 2.345113},
			scrollwheel: false,
			zoom: 13
		});
		infoWindow = new google.maps.InfoWindow({
			content: contentString
		});
		$.ajax({
			url: 'getPoints',
			type: 'POST',
			data: {'type': 'All'},
			dataType: 'json',
			success: function (json) {
				console.debug("success " + json);
				if (json.result == 'success') {
					//var markers = [];
					for (var i = 0; i < json.photos.length; i++) {
						var photo = json.photos[i];
						addMarker(photo.latitude, photo.longitude);
						//markers += marker;
					}
					//new MarkerClusterer(map, markers, {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
				}
				else {
					displayError(json.message)
				}
			},
			error: function () {
				displayError("Cannot connect to server")
			}
		});
        /* var marker1 = new google.maps.Marker({
         position: {lat: 48.862447, lng: 2.349093},
         label: "coucou"
         });
         var marker2 = new google.maps.Marker({
         position: {lat: 48.862235, lng: 2.356024},
         label: "caca"
         });
         var markers = [marker1, marker2];
         var markerCluster = new MarkerClusterer(map, markers,
         {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
         marker1.addListener('click', function() {
         map.setZoom(8);
         map.setCenter(marker.getPosition());
         });   */
	}
</script>
<script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCRoTk_Xjx_YTlXUWifpsBr4GnEcL2txJc&callback=initMap" async defer></script>
</body>
</html>
