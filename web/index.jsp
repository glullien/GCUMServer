<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GCUM</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/index.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="scripts/index.js"></script>
</head>
<body>
<div id="controls">
    <div class="controlBox">
        <span class="label">Afficher:</span><a href="#" id="display">tous</a>
    </div>
</div>
<div id="map"></div>
<div id="displayChoices">
    <a id="displayAll" href="#">Tous</a>
    <a id="displayLastDay" href="#">Dernier jour</a>
    <a id="displayLastWeek" href="#">Dernière semaine</a>
    <a id="displayLastMonth" href="#">Dernière mois</a>
</div>
<div id="photos">
    <p><a id="photosClose" href="#" class="close">&#10006;</a></p>
    <div id="photosList"></div>
</div>
<p>
    <span id="error"></span>
</p>
<script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCRoTk_Xjx_YTlXUWifpsBr4GnEcL2txJc&callback=initMap" async defer></script>
</body>
</html>
