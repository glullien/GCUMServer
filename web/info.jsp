<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <c:if test="${not gcum:isAdmin(sessionScope.sessionId)}">
        <meta http-equiv="refresh" content="0; url=index.jsp"/>
    </c:if>
    <title>GCUM Informations techniques</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-eye-open"></i> Carte</a>
        <a href="add.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-upload"></i> Ajouter</a>
        <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i> Extrait</a>
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
<%
    final Runtime runtime = Runtime.getRuntime();
    final int processors = runtime.availableProcessors();
    final long total = runtime.totalMemory();
    final long free = runtime.freeMemory();
    final long used = total - free;
    final long max = runtime.maxMemory();
    final long available = free + (max - total);
%>
<div style="margin-left: 20px; margin-top: 20px;">
    <p>Nombre de processors: <%=processors%> cœurs</p>
    <p>Mémoire max: <%=max%> (<%=max / 1048576%> Mb)</p>
    <p>Mémoire réservée: <%=total%> (<%=total / 1048576%> Mb)</p>
    <p>Mémoire utilisée: <%=used%> (<%=used / 1048576%> Mb)</p>
    <p>Mémoire libre: <%=free%> (<%=free / 1048576%> Mb)</p>
    <p>Mémoire dispo: <%=available%> (<%=available / 1048576%> Mb)</p>
</div>
</body>
</html>
