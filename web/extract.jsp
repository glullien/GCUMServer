<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <c:if test="${not gcum:isAdmin(sessionScope.sessionId)}">
        <meta http-equiv="refresh" content="0; url=index.jsp"/>
    </c:if>
    <title>GCUM Extraits</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/extract.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/extract.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-eye-open"></i> Carte</a>
        <a href="add.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-upload"></i> Ajouter</a>
        <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
        <div class="dropdown" style="display: inline; margin: 0; padding: 0;">
            <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                <i class="glyphicon glyphicon-user"></i>
                <span>${gcum:username(sessionScope.sessionId)}</span>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
                <li><a id="disconnect" href="#">Se déconnecter</a></li>
            </ul>
        </div>
    </div>
</div>
<div id="extractZone">
    <a href="extract?district=all" class="btn btn-primary">Tout</a>
    <a href="extract?district=1" class="btn btn-secondary">1er</a>
    <a href="extract?district=2" class="btn btn-secondary">2e</a>
    <a href="extract?district=3" class="btn btn-secondary">3e</a>
    <a href="extract?district=4" class="btn btn-secondary">4e</a>
    <a href="extract?district=5" class="btn btn-secondary">5e</a>
    <a href="extract?district=6" class="btn btn-secondary">6e</a>
    <a href="extract?district=7" class="btn btn-secondary">7e</a>
    <a href="extract?district=8" class="btn btn-secondary">8e</a>
    <a href="extract?district=9" class="btn btn-secondary">9e</a>
    <a href="extract?district=10" class="btn btn-secondary">10e</a>
    <a href="extract?district=11" class="btn btn-secondary">11e</a>
    <a href="extract?district=12" class="btn btn-secondary">12e</a>
    <a href="extract?district=13" class="btn btn-secondary">13e</a>
    <a href="extract?district=14" class="btn btn-secondary">14e</a>
    <a href="extract?district=15" class="btn btn-secondary">15e</a>
    <a href="extract?district=16" class="btn btn-secondary">16e</a>
    <a href="extract?district=17" class="btn btn-secondary">17e</a>
    <a href="extract?district=18" class="btn btn-secondary">18e</a>
    <a href="extract?district=19" class="btn btn-secondary">19e</a>
    <a href="extract?district=20" class="btn btn-secondary">20e</a>
</div>
</body>
</html>
