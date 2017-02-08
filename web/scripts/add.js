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
	$('#report').click(function (e) {
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
						$("#successModal").modal("show");
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

