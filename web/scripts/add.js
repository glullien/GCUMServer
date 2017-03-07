function setStreet(name) {
	$('#street').val(name);
	$('#streetZone').hide();
	$("#streetGroup").removeClass("has-error");
}
function setDistrict(name) {
	$('#district').val(name);
	$('#districtZone').hide();
	$("#districtGroup").removeClass("has-error");
}
function setDate(date) {
	$("#date").val(date);
	$("#dateGroup").removeClass("has-error");
}

function escapeQuote(source) {
	var res ="";
	for (var i = 0, len = source.length; i < len; i++) {
		var c = source[i];
		if (c == "'") res += "\\'";
		else res += c;
	}
	return res;
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
	var streetInput = $('#street');
	streetInput.on("input", function () {
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
							content += '<a href="#" onclick="setStreet(\'' + escapeQuote (s.name) + '\');return false;">' + s.name + '</a>';
						}
						$('#streets').html(content);
						$('#districtZone').hide();
						var offset = streetInput.offset();
						var streetZone = $('#streetZone');
						streetZone.css("top", ""+Math.max (40, offset.top-400)+"px");
						streetZone.css("left", ""+offset.left+"px");
						streetZone.css("width", "300px");
						streetZone.css("height", "400px");
						streetZone.css("position", "absolute");
						streetZone.show();
					}
				},
				error: function () {
				}
			});
		}
	});
	var
	districtInput = $('#district');
	districtInput.click(function () {
		$('#streetZone').hide();
		var offset = districtInput.offset();
		var districtZone = $('#districtZone');
		districtZone.css("top", ""+Math.max (40, offset.top-400)+"px");
		districtZone.css("left", ""+offset.left+"px");
		districtZone.css("height", "400px");
		districtZone.css("position", "absolute");
		districtZone.show();
	});
	$('#date').on("input", function () {
		var date = $('#date').val();
		var dateRegex = /^\d{4}-\d{2}-\d{2}$/;
		if (dateRegex.test(date)) $("#dateGroup").removeClass("has-error");
	});
	var postId = null;
	$('#fileupload').fileupload({
		url: "/upload",
		singleFileUploads: false,
		dataType: 'json',
		done: function (e, data) {
			var result = data.result;
			postId = result.id;
			var content = "";
			for (var i = 0; i < result.uploaded.length; i++) {
				var uploaded = result.uploaded[i];
				content += '<div class="photoAndLegend"><img src="getUploadedPhoto?id=' + uploaded.id + '&maxSize=330" class="photo"><br/>';
				if (uploaded.date != "unknown") content += '<a href="#" class="photoDate" onclick="setDate(\'' + uploaded.date + '\');return false;">' + uploaded.date + ' ' + uploaded.time + '</a>';
				if (uploaded.location != "unknown") content += '<span class="photoCoordinates">' + (uploaded.latitude / 1E5) + ' °N/' + (uploaded.longitude / 1E5) + ' °E</span>';
				if (uploaded.street != "unknown") content += '<a href="#" class="photoStreet" onclick="setStreet(\'' + uploaded.street + '\');setDistrict(\'' + uploaded.district + '\');return false;">' + uploaded.street + '</a>';
				if (uploaded.district != -1) content += '<span class="photoDistrict"> dans le ' + uploaded.district + 'e</span>';
				content += '</div>';
			}
			$('#uploadedProgressBar').css("width", '0%');
			$("#uploaded").html(content);
			$("#street").val(result.street);
			$("#district").val((result.district != -1) ? result.district : "");
			$("#date").val(result.date);
			$("#status").html(".");
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			console.debug("progress " + progress);
			$('#uploadedProgressBar').css("width", progress + '%');
		}
	});
	$('#report').click(function () {
		var street = $('#street').val();
		var district = $('#district').val();
		var date = $('#date').val();
		var dateRegex = /^\d{4}-\d{2}-\d{2}$/;
		if (street == "") $("#streetGroup").addClass("has-error");
		else $("#streetGroup").removeClass("has-error");
		if (district == "") $("#districtGroup").addClass("has-error");
		else $("#districtGroup").removeClass("has-error");
		if (!dateRegex.test(date)) $("#dateGroup").addClass("has-error");
		else $("#dateGroup").removeClass("has-error");
		if (postId == null) $("#status").html("Ajouter des photos d'abord");
		else if (street == "") $("#status").html("Précisez la rue");
		else if (district == "") $("#status").html("Précisez l'arrondissement");
		else if (!dateRegex.test(date)) $("#status").html("Précisez la date");
		else {
			$('#report').prop("disabled", true);
			$("#status").html("Mise à jour de la base de données");
			$.ajax({
				url: 'reportUploaded',
				type: 'POST',
				data: {'id': postId, 'street': street, 'district': district, 'date': date},
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') {
						$("#uploaded").html("");
						$("#street").val("");
						$("#district").val("");
						$("#date").val("");
						$("#status").html(".");
						$("#successModal").modal("show");
					}
					else {
						$("#status").html(json.message);
					}
					$('#report').prop("disabled", false);
				},
				error: function () {
					$("#status").html("Internal error");
					$('#report').prop("disabled", false);
				}
			});
		}
	});
});

