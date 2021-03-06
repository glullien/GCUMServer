<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <title>Informations techniques</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="icon" type="image/png" href="images/GCUM2-32.png" />
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/info.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <script type="text/javascript">
			autoLogin();
        </script>
    </c:if>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-map-marker"></i><span class="hideOnMobile link">Carte</span></a>
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
                        <li class="showOnMobile"><a href="#" id="accountName">Compte de ${gcum:username(sessionScope.sessionId)}</a></li>
                        <li><a href="changePassword.jsp">Changer le mot de passe</a></li>
                        <li><a href="changeEmail.jsp">Changer d'adresse email</a></li>
                        <li><a href="notifications.jsp">Changer les notifications</a></li>
                        <li><a id="disconnect" href="#">Se déconnecter</a></li>
                    </ul>
                </div>
            </c:when>
            <c:otherwise>
                <a href="login.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-user"></i><span class="hideOnMobile">Se connecter</span></a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<div style="margin: 20px;">
    <p>Version ${gcum:versionName()}</p>
    <p>
        Ce site vous permet librement, gratuitement de publier et partager en direct vos photos de véhicules mal garées sur Paris.
        Vous pouvez y déposer les photos de voitures, camions, scooters, motos et même vélos garés illégalement sur un
        emplacement qui leur sont interdits : pistes cyclables, trottoir, passage piétons...
    </p>

    <p>
        Pour cela vous devez <a href="login.jsp">créer</a> un compte totalement anonyme, puis ajouter vos photos via le site,
        ou l'application <a href="https://play.google.com/store/apps/details?id=gcum.gcumfisher">Android</a>.
    </p>

    <p>
        N'hésitez à nous suivre sur <a href="https://twitter.com/gcum_lol">Twitter</a> !
    </p>

    <p>
        <a href="https://play.google.com/store/apps/details?id=gcum.gcumfisher">
            <img src="images/Android-x110.png" style="margin: 30px" class="smallerIconOnMobile">
        </a>
        <a href="https://twitter.com/gcum_lol">
            <img src="images/Twitter-x110.png" style="margin: 30px" class="smallerIconOnMobile">
        </a>
    </p>
    <c:if test="${gcum:isAdmin(sessionScope.sessionId)}">
        <%
            final Runtime runtime = Runtime.getRuntime();
            final int processors = runtime.availableProcessors();
            final long total = runtime.totalMemory();
            final long free = runtime.freeMemory();
            final long used = total - free;
            final long max = runtime.maxMemory();
            final long available = free + (max - total);
        %>
        <p>Nombre de processors: <%=processors%> cœurs</p>
        <p>Mémoire max: <%=max%> (<%=max / 1048576%> Mb)</p>
        <p>Mémoire réservée: <%=total%> (<%=total / 1048576%> Mb)</p>
        <p>Mémoire utilisée: <%=used%> (<%=used / 1048576%> Mb)</p>
        <p>Mémoire libre: <%=free%> (<%=free / 1048576%> Mb)</p>
        <p>Mémoire disponible: <%=available%> (<%=available / 1048576%> Mb)</p>
    </c:if>
</div>
</body>
</html>
