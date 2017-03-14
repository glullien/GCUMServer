<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <meta http-equiv="refresh" content="0; url=index.jsp"/>
    </c:if>
    <title>Ajouter GCUM</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/add.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-3.1.1.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="lib/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="lib/jquery.fileupload.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
    <script type="text/javascript" src="scripts/add.js"></script>
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
        <c:if test="${gcum:isAdmin(sessionScope.sessionId)}">
            <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i><span
                    class="hideOnMobile link">Extrait</span></a>
        </c:if>
        <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
        <div class="dropdown" style="display: inline; margin: 0; padding: 0;top: -1px">
            <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                <i class="glyphicon glyphicon-user"></i>
                <span class="hideOnMobile">${gcum:username(sessionScope.sessionId)}</span>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
                <li class="showOnMobile"><a href="#" id="accountName">Compte de ${gcum:username(sessionScope.sessionId)}</a></li>
                <li><a href="changePassword.jsp">Changer le mot de passe</a></li>
                <li><a href="changeEmail.jsp">Changer d'adresse email</a></li>
                <li><a id="disconnect" href="#">Se déconnecter</a></li>
            </ul>
        </div>
    </div>
</div>
<div id="uploadZone">
<span class="btn btn-success fileinput-button">
     <i class="glyphicon glyphicon-camera"></i>
    <span>Sélectionner les photos (JPG)...</span>
    <input id="fileupload" type="file" name="files[]" multiple>
</span>
</div>
<div id="uploadedZone">
    <div class="progress" id="uploadedProgress">
        <div class="progress-bar" role="progressbar" style="width:0" id="uploadedProgressBar">
            <span class="sr-only">70% Complete</span>
        </div>
    </div>
    <div id="uploaded"></div>
</div>

<form class="form-inline">
    <div id="reportZone">
        <div class="form-group" id="streetGroup">
            <label for="street">Lieu :</label>
            <input type="text" id="number" class="form-control" title="Numéro de voie">
            <input type="text" id="street" class="form-control">
        </div>
        <div class="form-group" id="dateGroup">
            <label for="date">Date :</label>
            <input type="text" id="date" class="form-control" placeholder="yyyy-mm-dd">
        </div>
        <div class="form-group" id="timeGroup">
            <label for="time">Heure :</label>
            <input type="text" id="time" class="form-control" placeholder="hh:mm:ss">
        </div>
        <button type="button" id="report" class="btn btn-success btn-lg"><i class="glyphicon glyphicon-cloud-upload"></i> Envoyer</button>
        <div class="alert alert-warning">
            <span id="status"></span>
        </div>
    </div>
</form>

<div class="modal fade" id="successModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <p>Photos archivées avec succès !</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-success" data-dismiss="modal">Ok</button>
            </div>
        </div>
    </div>
</div>

<div id="streetZone">
    <div id="streets"></div>
    <a id="streetsClose" href="#" class="close">&#10006;</a>
</div>

<div id="dateZone">
    <p><span id="dateClose" class="close">&#10006;</span></p>
    <table>
        <tr>
            <td>
                <button type="button" class="btn btn-outline-primary" id="prevMonth"><i class="glyphicon glyphicon-triangle-left"></i></button>
            </td>
            <td colspan="5" style="text-align: center;"><span id="month">Mars 2017</span></td>
            <td>
                <button type="button" class="btn btn-outline-primary" id="nextMonth"><i class="glyphicon glyphicon-triangle-right"></i></button>
            </td>
        </tr>
        <tr>
            <td>Di</td>
            <td>Lu</td>
            <td>Ma</td>
            <td>Me</td>
            <td>Je</td>
            <td>Ve</td>
            <td>Sa</td>
        </tr>
        <c:forEach var="line" begin="0" end="5">
            <tr>
                <c:forEach var="col" begin="0" end="6">
                    <td style="text-align: center">
                        <button type="button" class="btn btn-default" id="date${line*7+col}" style="width: 100%; border-radius: 0;">${line*7+col}</button>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>
</div>
<div id="timeZone">
    <p><span id="timeClose" class="close">&#10006;</span></p>
    <table style="width: 100%;">
        <c:forEach var="line" begin="0" end="3">
            <tr>
                <c:forEach var="col" begin="0" end="5">
                    <td style="text-align: center">
                        <button type="button" class="btn btn-default" id="hour${line*6+col}" style="width: 100%; border-radius: 0;">${line*6+col}h</button>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>
    <table style="width: 100%; margin-top: 10px;">
        <tr>
            <c:forEach var="col" begin="0" end="3">
                <td style="text-align: center">
                    <button type="button" class="btn btn-default" id="minute${col*15}" style="width: 100%; border-radius: 0;">${col*15}mn</button>
                </td>
            </c:forEach>
        </tr>
    </table>
    <button type="button" class="btn btn-primary" id="timeApply" style="width: 100%; margin-top: 10px;">Ok</button>
</div>

</body>
</html>
