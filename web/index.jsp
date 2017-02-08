<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GCUM</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/index.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/index.js"></script>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="controlBox">
        <form class="form-inline" style="margin: 0; padding: 0;">
            <div class="form-group">
                <span>Dates :</span>
                <div class="dropdown" style="display: inline;">
                    <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                        <span id="date">Tout</span>
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a id="dateAll" href="#">Tout</a></li>
                        <li><a id="dateLastDay" href="#">Dernier jour</a></li>
                        <li><a id="dateLastWeek" href="#">Dernière semaine</a></li>
                        <li><a id="dateLastMonth" href="#">Dernière mois</a></li>
                    </ul>
                </div>
            </div>
            <div class="form-group">
                <span>Précision géographique :</span>
                <div class="dropdown" style="display: inline;">
                    <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                        <span id="locationSource">Tout</span>
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a id="locationSourceAll" href="#">Tout</a></li>
                        <li><a id="locationSourceGPS" href="#">Par GPS</a></li>
                    </ul>
                </div>
            </div>
        </form>
    </div>
    <div class="links">
        <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i> Extrait</a>
        <a href="add.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-upload"></i> Ajouter</a>
    </div>
</div>
<div id="map"></div>

<div id="photos">
    <p><a id="photosClose" href="#" class="close">&#10006;</a></p>
    <div id="photosList"></div>
</div>
<p>
    <span id="error"></span>
</p>
<script src="lib/markerclusterer.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCRoTk_Xjx_YTlXUWifpsBr4GnEcL2txJc&callback=initMap" async defer></script>
</body>
</html>
