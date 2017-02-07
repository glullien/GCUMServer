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
	$('#successZone').hide();
	$('#successClose').click(function () {
		$('#successZone').hide();
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
		},
		progress: function (e) {
			console.debug("progress " + e);
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
						$("#uploaded").html("");
						$("#street").val("");
						$("#district").val("");
						$("#date").val("");
						$("#status").html(".");
						$("#successZone").show();
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

