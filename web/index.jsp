<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <title>GCUM</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/index.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
    <script type="text/javascript" src="scripts/index.js"></script>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <script type="text/javascript">
			autoLogin();
        </script>
    </c:if>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="controlBox">
        <form class="form-inline" style="margin: 0; padding: 0;">
            <div class="form-group">
                <span class="hideOnMobile">Dates :</span>
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
                <span class="hideOnMobile">Précision géographique :</span>
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
            <div class="form-group">
                <span class="hideOnMobile">Publiés par :</span>
                <div class="dropdown" style="display: inline;">
                    <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                        <span id="authors">Tous</span>
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a id="authorsAll" href="#">Tous</a></li>
                        <c:if test="${gcum:isLogin(sessionScope.sessionId)}">
                            <li><a id="authorsMyself" href="#">Moi-même</a></li>
                        </c:if>
                    </ul>
                </div>
            </div>
        </form>
    </div>
    <div class="links">
        <a href="list.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-align-justify"></i><span class="hideOnMobile link">Liste</span></a>
        <c:choose>
            <c:when test="${gcum:isLogin(sessionScope.sessionId)}">
                <a href="add.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-upload"></i><span class="hideOnMobile link">Ajouter</span></a>
                <c:if test="${gcum:isAdmin(sessionScope.sessionId)}">
                    <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i><span class="hideOnMobile link">Extrait</span></a>
                </c:if>
                <div class="dropdown" style="display: inline; margin: 0; padding: 0; top: -1px;">
                    <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="glyphicon glyphicon-user"></i>
                        <span class="hideOnMobile">${gcum:username(sessionScope.sessionId)}</span>
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-right">
                        <li><a href="changePassword.jsp">Changer le mot de passe</a></li>
                        <li><a href="changeEmail.jsp">Changer d'adresse email</a></li>
                        <li><a id="disconnect" href="#">Se déconnecter</a></li>
                    </ul>
                </div>
            </c:when>
            <c:otherwise>
                <a href="login.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-user"></i><span class="hideOnMobile link">Se connecter</span></a>
            </c:otherwise>
        </c:choose>
        <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
    </div>
</div>
<div id="map"></div>

<div id="photos">
    <p><a id="photosClose" href="#" class="close">&#10006;</a></p>
    <div id="photosList"></div>
</div>
<div id="android">
    <a id="androidClose" href="#" class="close">&#10006;</a>
    <div id="androidMessage">
        <p>Téléchargez l'appli !</p>
        <p><a href="https://play.google.com/store/apps/details?id=gcum.gcumfisher"><img src="images/Android.png"></a></p>
        <p><button class="btn btn-outline-primary" id="doNotDisplayAndroid">Ne plus afficher</button></p>
    </div>
</div>
<p>
    <span id="error"></span>
</p>
<script src="lib/markerclusterer.js"></script>
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCRoTk_Xjx_YTlXUWifpsBr4GnEcL2txJc&callback=initMap" async defer></script>
</body>
</html>
