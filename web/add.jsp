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
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/add.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="lib/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="lib/jquery.fileupload.js"></script>
    <script type="text/javascript" src="scripts/add.js"></script>
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
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-eye-open"></i> Carte</a>
        <c:if test="${gcum:isAdmin(sessionScope.sessionId)}">
            <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i> Extrait</a>
            <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
        </c:if>
        <div class="dropdown" style="display: inline; margin: 0; padding: 0;top: -1px">
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
<div id="uploadZone">
<span class="btn btn-success fileinput-button">
     <i class="glyphicon glyphicon-camera"></i>
    <span>Sélectionner les photos (JPG)...</span>
    <input id="fileupload" type="file" name="files[]" multiple>
</span>
</div>
<div id="uploadedZone">
    <div class="progress" id="uploadedProgress">
        <div class="progress-bar" role="progressbar" style="width:0%" id="uploadedProgressBar">
            <span class="sr-only">70% Complete</span>
        </div>
    </div>
    <div id="uploaded"></div>
</div>

<form class="form-inline">
    <div id="reportZone">
        <div class="form-group" id="streetGroup">
            <label for="street">Rue :</label>
            <input type="text" id="street" class="form-control">
        </div>
        <div class="form-group" id="districtGroup">
            <label for="district">Arrondissement :</label>
            <input type="text" id="district" class="form-control">
        </div>
        <div class="form-group" id="dateGroup">
            <label for="date">Date :</label>
            <input type="text" id="date" class="form-control">
            <span id="dateHelp">yyyy-mm-dd</span>
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

<div id="districtZone">
    <div id="districts">
        <a href="#" onclick="setDistrict('1er');return false;">1er</a>
        <a href="#" onclick="setDistrict('2e');return false;">2e</a>
        <a href="#" onclick="setDistrict('3e');return false;">3e</a>
        <a href="#" onclick="setDistrict('4e');return false;">4e</a>
        <a href="#" onclick="setDistrict('5e');return false;">5e</a>
        <a href="#" onclick="setDistrict('6e');return false;">6e</a>
        <a href="#" onclick="setDistrict('7e');return false;">7e</a>
        <a href="#" onclick="setDistrict('8e');return false;">8e</a>
        <a href="#" onclick="setDistrict('9e');return false;">9e</a>
        <a href="#" onclick="setDistrict('10e');return false;">10e</a>
        <a href="#" onclick="setDistrict('11e');return false;">11e</a>
        <a href="#" onclick="setDistrict('12e');return false;">12e</a>
        <a href="#" onclick="setDistrict('13e');return false;">13e</a>
        <a href="#" onclick="setDistrict('14e');return false;">14e</a>
        <a href="#" onclick="setDistrict('15e');return false;">15e</a>
        <a href="#" onclick="setDistrict('16e');return false;">16e</a>
        <a href="#" onclick="setDistrict('17e');return false;">17e</a>
        <a href="#" onclick="setDistrict('18e');return false;">18e</a>
        <a href="#" onclick="setDistrict('19e');return false;">19e</a>
        <a href="#" onclick="setDistrict('20e');return false;">20e</a>
    </div>
    <a id="districtsClose" href="#" class="close">&#10006;</a>
</div>
</body>
</html>
