<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Ajouter GCUM</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/add.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="lib/jquery.fileupload.js"></script>
    <script type="text/javascript" src="scripts/add.js"></script>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i> Extrait</a>
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-eye-open"></i> Carte</a>
    </div>
</div>
<div id="uploadZone">
<span class="btn btn-success fileinput-button">
     <i class="glyphicon glyphicon-plus"></i>
    <span>Ajouter une photo (JPG)...</span>
    <input id="fileupload" type="file" name="files[]" multiple>
</span>
</div>
<div id="uploaded">

</div>

<form class="form-inline">
    <div id="reportZone">
        <div class="form-group">
            <label for="street">Rue :</label>
            <input type="text" id="street" class="form-control">
        </div>
        <div class="form-group">
            <label for="district">Arrondissement :</label>
            <input type="text" id="district" class="form-control">
        </div>
        <div class="form-group">
            <label for="date">Date :</label>
            <input type="text" id="date" class="form-control">
            <span id="dateHelp">yyyy-mm-dd</span>
        </div>
        <button type="button" id="report" class="btn btn-success btn-lg"><i class="glyphicon glyphicon-cloud-upload"></i> Envoyer</button>
    </div>
</form>
<span id="status">.</span>

<div id="successZone">
    Photos archivées avec succès !
    <a id="successClose" href="#">Ok</a>
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
