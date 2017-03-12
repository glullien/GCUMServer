<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <title>GCUM</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/photo.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/list.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
    <script type="text/javascript" src="scripts/photo.js"></script>
    <script type="text/javascript" src="scripts/list.js"></script>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <script type="text/javascript">
			autoLogin();
        </script>
    </c:if>
</head>
<body>
<div class="frames">
    <div id="list" class="framesList"></div>
    <div class="framesList">
        <div class="frame">
            <a href="#more" class="btn btn-link btn-lg" id="more">
                <i class="glyphicon glyphicon-plus"></i>
                Plus (<span id="nbAfter">.</span>)
            </a>
        </div>
    </div>
</div>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="hideOnSmallScreen controlBox">
        <form class="form-inline" style="margin: 0; padding: 0;">
            <div class="form-group">
                <span class="hideOnMobile">Arrondissements</span>
                <div class="dropdown" style="display: inline;">
                    <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                        <span id="district">Tout</span>
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a id="districtAll" href="#">Tous</a></li>
                        <li><a id="district1" href="#">1er</a></li>
                        <c:forEach var="d" begin="2" end="20">
                            <li><a id="district${d}" href="#">${d}e</a></li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </form>
    </div>
    <div class="showOnSmallScreen controlBox">
        <a href="#" class="btn btn-outline-primary" id="filterOpen"><i class="glyphicon glyphicon glyphicon-filter"></i><span
                class="hideOnMobile link">Filter</span></a>
    </div>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-map-marker"></i><span class="hideOnMobile link">Carte</span></a>
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
                        <li class="showOnMobile"><a href="#" id="accountName">Compte de ${gcum:username(sessionScope.sessionId)}</a></li>
                        <li><a href="changePassword.jsp">Changer le mot de passe</a></li>
                        <li><a href="changeEmail.jsp">Changer d'adresse email</a></li>
                        <li><a href="notifications.jsp">Changer les notifications</a></li>
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

<div id="filter">
    <p><span id="filterClose" class="close">&#10006;</span></p>
    <div id="filterPane">
        <form>
            <div class="form-group">
                <label for="filterDistrict">Arrondissements:</label>
                <select id="filterDistrict" class="form-control">
                    <option value="All">Tous</option>
                    <option value="1">1er</option>
                    <c:forEach var="d" begin="2" end="20">
                        <option value="${d}">${d}e</option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="btn btn-default" id="filterApply">Filtrer</button>
        </form>
    </div>
</div>

<div id="photo">
    <p><span id="photoClose" class="close">&#10006;</span></p>
    <div id="photoPanes">
        <div id="photoPane"><img id="photoImg" src="settings-512.png"></div>
        <div id="photoDetails">
            <p>Photo prise par <span id="photoAuthor"></span></p>
            <p>le <span id="photoDate"></span></p>
            <p><span id="photoNumber"></span>  <span id="photoStreet"></span> dans le <span id="photoDistrict"></span></p>
            <p>Latitude <span id="photoLatitude"></span></p>
            <p>Longitude <span id="photoLongitude"></span></p>
            <p>Taille <span id="photoSize"></span></p>
            <p>
                <a href="#" download="A" id="photoDownload"><i class="glyphicon glyphicon-cloud-download"></i> Download</a>
                <a href="#" id="photoView"><i class="glyphicon glyphicon-eye-open"></i> View</a>
            </p>
            <p>Aimé par <span id="photoLikes"></span></p>
        </div>
    </div>
</div>
<p>
    <span id="error"></span>
</p>
</body>
</html>
