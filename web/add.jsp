<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Ajouter GCUM</title>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/add.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="lib/jquery.fileupload.js"></script>
</head>
<body>
<div id="controls">
    <div class="links">
        <a href="extract.jsp">Extrait</a>
        <a href="index.jsp">Carte</a>
    </div>
</div>
<div id="uploadZone">
<span class="btn btn-success fileinput-button">
     <i class="glyphicon glyphicon-plus"></i>
    <span>Select files...</span>
    <input id="fileupload" type="file" name="files[]" multiple>
</span>
</div>
<div id="uploaded">

</div>

<div id="reportZone">
    <label for="street">Rue :</label>
    <input type="text" id="street">
    <label for="district">Arrondissement :</label>
    <input type="text" id="district">
    <label for="date">Date :</label>
    <input type="text" id="date">
    <span id="dateHelp">yyyy-mm-dd</span>
    <a href="#" id="report">Envoyer</a>
</div>
<span id="status">.</span>

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

<script>
	function setStreet(name) {
		$('#street').val(name);
		$('#streetZone').hide();
	}
	function setDistrict(name) {
		$('#district').val(name);
		$('#districtZone').hide();
	}
	$(function () {
		$('#streetZone').hide();
		$('#streetsClose').click(function () {
			$('#streetZone').hide();
		});
		$('#districtZone').hide();
		$('#districtsClose').click(function () {
			$('#districtZone').hide();
		});
		$('#street').on("input", function () {
			var street = $('#street').val();
			if (street.length < 2) $('#streetZone').hide();
			else {
				$.ajax({
					url: 'searchStreet',
					type: 'POST',
					data: {'nbAnswers': 25, 'pattern': street},
					dataType: 'json',
					success: function (json) {
						if (json.result == 'success') {
							var content = "";
							for (var i = 0; i < json.streets.length; i++) {
								var s = json.streets[i];
								content += '<a href="#" onclick="setStreet(\'' + s.name + '\');return false;">' + s.name + '</a>';
							}
							$('#streets').html(content);
							$('#districtZone').hide();
							$('#streetZone').show();
						}
					},
					error: function () {
					}
				});
			}
		});
		$('#district').click(function (e) {
			$('#streetZone').hide();
			$('#districtZone').show();
		});
		var postId = null;
		$('#fileupload').fileupload({
			url: "/upload",
			dataType: 'json',
			done: function (e, data) {
				var result = data.result;
				postId = result.id;
				var content = "";
				for (var i = 0; i < result.uploaded.length; i++) {
					var uploaded = result.uploaded[i];
					content += '<div class="photoAndLegend"><img src="getUploadedPhoto?id=' + uploaded.id + '&maxSize=400" class="photo"><br/>';
					if (uploaded.date != "unknown") content += '<span class="photoDate">' + uploaded.date + ' ' + uploaded.time + '</span>';
					if (uploaded.location != "unknown") content += '<span class="coordinates">' + (uploaded.latitude / 1E5) + '/' + (uploaded.longitude / 1E5) + '</span>';
					content += '<br/>';
					if (uploaded.street != "unknown") content += '<span class="street">' + uploaded.street + '</span>';
					if (uploaded.district != -1) content += '<span class="district"> dans le ' + uploaded.district + 'e</span>';
					content += '</div>';
				}
				$("#uploaded").html(content);
				$("#street").val(result.street);
				$("#district").val((result.district != -1) ? result.district : "");
				$("#date").val(result.date);
				$("#status").html(".");
			}
		});
		$('#report').click(function (e) {
			var street = $('#street').val();
			var district = $('#district').val();
			var date = $('#date').val();
			if (postId == null) $("#status").html("Ajouter des photos d'abord");
			else if (street == "") $("#status").html("Précisez la rue");
			else if (district == "") $("#status").html("Précisez l'arrondissement");
			else if (date == "") $("#status").html("Précisez la date");
			else $.ajax({
					url: 'reportUploaded',
					type: 'POST',
					data: {'id': postId, 'street': street, 'district': district, 'date': date},
					dataType: 'json',
					success: function (json) {
						if (json.result == 'success') {
							var content = "";
							for (var i = 0; i < json.streets.length; i++) {
								var s = json.streets[i];
								content += '<a href="#" onclick="setStreet(\'' + s.name + '\');return false;">' + s.name + '</a>';
							}
							$('#streets').html(content);
							$('#districtZone').hide();
							$('#streetZone').show();
						}
						else {
							$("#status").html(json.message);
						}
					},
					error: function () {
						$("#status").html("Internal error");
					}
				});
		});
	});
</script>
</body>
</html>
