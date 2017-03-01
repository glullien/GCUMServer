<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <title>GCUM</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/list.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
    <script type="text/javascript" src="scripts/list.js"></script>
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
                <span>Arrondissements</span>
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
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-eye-open"></i> Carte</a>
        <c:choose>
            <c:when test="${gcum:isLogin(sessionScope.sessionId)}">
                <a href="add.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-upload"></i> Ajouter</a>
                <c:if test="${gcum:isAdmin(sessionScope.sessionId)}">
                    <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i> Extrait</a>
                    <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
                </c:if>
                <div class="dropdown" style="display: inline; margin: 0; padding: 0; top: -1px;">
                    <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="glyphicon glyphicon-user"></i>
                        <span>${gcum:username(sessionScope.sessionId)}</span>
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
                <a href="login.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-user"></i> Se connecter</a>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<div class="frames">
    <div id="list"></div>
    <div class="frame">
        <a href="#more" class="btn btn-link btn-lg" id="more"><i class="glyphicon glyphicon-plus"></i> Plus</a>
    </div>
</div>

<div id="photo">
    <p><span id="photoClose" class="close">&#10006;</span></p>
    <div id="photoPanes">
        <div id="photoPane"><img id="photoImg" src="settings-512.png"></div>
        <div id="photoDetails">
            <p>Photo prise par <span id="photoAuthor"></span></p>
            <p>le <span id="photoDate"></span></p>
            <p><span id="photoStreet"></span> dans le <span id="photoDistrict"></span></p>
            <p>Latitude <span id="photoLatitude"></span></p>
            <p>Longitude <span id="photoLongitude"></span></p>
            <p>Taille <span id="photoSize"></span></p>
            <p><a href="#" download="A" id="photoDownload"><i class="glyphicon glyphicon-cloud-download"></i> Download</a></p>
            <p>Aimé par <span id="photoLikes"></span></p>
        </div>
    </div>
</div>
<p>
    <span id="error"></span>
</p>
</body>
</html>
