var street = "";
var district = "";
function setStreet(text, newStreet, newDistrict) {
	$('#street').val(text);
	$('#streetZone').hide();
	$("#streetGroup").removeClass("has-error");
	street = newStreet;
	district = newDistrict;
}
function setNumber(number) {
	$("#number").val(number);
}
function setDate(date) {
	$("#date").val(date);
	$("#dateGroup").removeClass("has-error");
}

function escapeQuote(source) {
	var res = "";
	for (var i = 0, len = source.length; i < len; i++) {
		var c = source[i];
		if (c == "'") res += "\\'";
		else res += c;
	}
	return res;
}

var months = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];

var month, year;
function updateCalendar() {
	$("#month").html(months[month] + " " + (year + 1900));
	var first = new Date(1900 + year, month, 1).getDay();
	var daysInMonth = new Date(1900 + year, month + 1, 0).getDate();
	var today = new Date();
	for (var i = 0; i < 42; i++) {
		var dateCell = $("#date" + i);
		if ((i < first) || (i >= first + daysInMonth)) dateCell.hide();
		else {
			dateCell.show();
			var d = (1 + i - first);
			dateCell.html("" + d);
			if ((today.getMonth() == month) && (today.getYear() == year) && (today.getDate() == d)) {
				dateCell.removeClass("btn-default");
				dateCell.addClass("btn-primary");
			}
			else {
				dateCell.removeClass("btn-primary");
				dateCell.addClass("btn-default");
			}
		}
	}
}

function chooseDate(id) {
	var first = new Date(1900 + year, month, 1).getDay();
	var day = 1 + parseInt(id.substring(4)) - first;
	$("#date").val("" + (year + 1900) + "-" + ((month < 9) ? "0" : "") + (month + 1) + "-" + ((day < 10) ? "0" : "") + day);
	$("#dateGroup").removeClass("has-error");
}

var lastHour = null;
var lastMinute = null;

$(function () {
	$('#streetsClose').click(function () {
		$('#streetZone').hide();
	});
	$('#dateClose').click(function () {
		$('#dateZone').hide();
	});
	$('#timeClose').click(function () {
		$('#timeZone').hide();
	});
	for (var i = 0; i < 42; i++) {
		$("#date" + i).click(function (e) {
			chooseDate(e.target.id);
			$('#dateZone').hide();
		})
	}
	for (var j = 0; j < 24; j++) {
		$("#hour" + j).click(function (e) {
			if (lastHour != null) {
				lastHour.removeClass("btn-info");
				lastHour.addClass("btn-default");
			}
			lastHour = $("#" + e.target.id);
			lastHour.removeClass("btn-default");
			lastHour.addClass("btn-info");
			if (lastMinute != null) {
				$("#timeApply").removeClass("disabled");
			}
		})
	}
	for (var k = 0; k < 4; k++) {
		$("#minute" + (k * 15)).click(function (e) {
			if (lastMinute != null) {
				lastMinute.removeClass("btn-info");
				lastMinute.addClass("btn-default");
			}
			lastMinute = $("#" + e.target.id);
			lastMinute.removeClass("btn-default");
			lastMinute.addClass("btn-info");
			if (lastHour != null) {
				$("#timeApply").removeClass("disabled");
			}
		})
	}
	$("#timeApply").click(function () {
		if ((lastHour != null) && (lastMinute != null)) {
			var hour = parseInt(lastHour.attr('id').substring(4));
			var minute = parseInt(lastMinute.attr('id').substring(6));
			$("#time").val("" + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":00");
			$("#timeZone").hide();
		}
	});
	var streetInput = $('#street');
	streetInput.on("input", function () {
		var street = $('#street').val();
		if (street.length < 2) $('#streetZone').hide();
		else {
			$.ajax({
				url: 'searchAddress',
				type: 'POST',
				data: {'answerCharset': 'UTF-8', 'nbAnswers': 25, 'pattern': street},
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') {
						var content = "";
						for (var i = 0; i < json.streets.length; i++) {
							var s = json.streets[i];
							var text = s.street + ', dans le ' + s.district + (s.district == 1 ? 'er' : 'e');
							content += '<a href="#" onclick="setStreet(\'' + escapeQuote(text) + '\', \'' + escapeQuote(s.street) + '\', \'' + s.district + '\');return false;">' + text + '</a>';
						}
						$('#streets').html(content);
						$('#dateZone').hide();
						$('#timeZone').hide();
						var offset = streetInput.offset();
						var streetZone = $('#streetZone');
						streetZone.css("top", "" + Math.max(40, offset.top - 400) + "px");
						streetZone.css("left", "" + offset.left + "px");
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
	var dateInput = $('#date');
	dateInput.click(function () {
		$('#streetZone').hide();
		$('#timeZone').hide();
		var offset = dateInput.offset();
		var dateZone = $('#dateZone');
		dateZone.css("top", "" + Math.max(40, offset.top - 310) + "px");
		dateZone.css("left", "" + Math.min(offset.left, $(window).width() - (dateZone.width() + 12 * 2)) + "px");
		dateZone.css("height", "310px");
		dateZone.css("position", "absolute");
		dateZone.show();
		var today = new Date();
		month = today.getMonth();
		year = today.getYear();
		updateCalendar();
	});
	$("#prevMonth").click(function () {
		if (month > 0) month = month - 1;
		else {
			year = year - 1;
			month = 11;
		}
		updateCalendar();
	});
	$("#nextMonth").click(function () {
		if (month < 11) month = month + 1;
		else {
			year = year + 1;
			month = 0;
		}
		updateCalendar();
	});
	var timeInput = $('#time');
	timeInput.click(function () {
		$('#streetZone').hide();
		$('#dateZone').hide();
		var offset = timeInput.offset();
		var timeZone = $('#timeZone');
		timeZone.css("top", "" + Math.max(40, offset.top - 265) + "px");
		timeZone.css("left", "" + Math.min(offset.left, $(window).width() - (timeZone.width() + 12 * 2)) + "px");
		timeZone.css("height", "265px");
		timeZone.css("position", "absolute");
		if (lastHour != null) {
			lastHour.removeClass("btn-info");
			lastHour.addClass("btn-default");
		}
		if (lastMinute != null) {
			lastMinute.removeClass("btn-info");
			lastMinute.addClass("btn-default");
		}
		lastHour = null;
		lastMinute = null;
		$("#timeApply").addClass("disabled");
		timeZone.show();
	});
	/*$('#date').on("input", function () {
	 var date = $('#date').val();
	 var dateRegex = /^\d{4}-\d{2}-\d{2}$/;
	 if (dateRegex.test(date)) $("#dateGroup").removeClass("has-error");
	 }); */
	$("#uploaded").html("");
	var postId = null;
	$('#fileupload').fileupload({
		url: "/upload",
		formData: {answerCharset: 'UTF-8'},
		singleFileUploads: false,
		fail: function(e, data) {
			console.log('Fail!');
		},
		done: function (e, data) {
			var result = data.result;
			postId = result.id;
			var content = "";
			for (var i = 0; i < result.uploaded.length; i++) {
				var uploaded = result.uploaded[i];
				var target = targetSize(uploaded.width, uploaded.height, 330);
				content += '<div class="photoAndLegend">';
				content += '<div class="photo">';
				content += '<p class="waitingPhoto" style="line-height: ' + target.height + 'px;"><i class="glyphicon glyphicon-refresh spinning"></i></p>';
				content += '<img width="' + target.width + '" height="' + target.height + '" src="getUploadedPhoto?id=' + uploaded.id + '&maxSize=330" class="photoImage">';
				content += '</div>';
				if (uploaded.date != "unknown") content += '<a href="#" class="photoDate" onclick="setDate(\'' + uploaded.date + '\');return false;">' + uploaded.date + ' ' + uploaded.time + '</a>';
				if (uploaded.location != "unknown") content += '<span class="photoCoordinates">' + (uploaded.latitude / 1E5) + ' °N/' + (uploaded.longitude / 1E5) + ' °E</span>';
				if (uploaded.number != "unknown") content += '<span class="photoNumber">' + uploaded.number + ', </span>';
				if (uploaded.street != "unknown") {
					var text = uploaded.street + ', dans le ' + uploaded.district + (uploaded.district == 1 ? 'er' : 'e');
					var link = 'setStreet(\'' + text + '\', \'' + uploaded.street + '\', \'' + uploaded.district + '\');return false;';
					if (uploaded.number != "unknown") link = 'setNumber(\'' + uploaded.number + '\');' + link;
					content += '<a href="#" class="photoStreet" onclick="' + link + '">' + uploaded.street + '</a>';
				}
				if (uploaded.district != -1) content += '<span class="photoDistrict"> dans le ' + uploaded.district + 'e</span>';
				content += '</div>';
			}
			$('#uploadedProgressBar').css("width", '0%');
			$("#uploaded").html(content);
			$("#number").val((result.number != "unknown") ? result.number : "");
			$("#street").val((result.street != "unknown") ? (result.street + ', dans le ' + result.district + (result.district == 1 ? 'er' : 'e')) : "");
			street = result.street;
			district = result.district;
			$("#date").val((result.date != "unknown") ? result.date : "");
			$("#time").val((result.time != "unknown") ? result.time : "");
			$("#status").html(".");
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			$('#uploadedProgressBar').css("width", progress + '%');
			if (data.loaded == data.total) {
				$("#uploaded").html("Analyse des images...");
			}
		}
	});
	$('#report').click(function () {
		var number = $("#number").val();
		var date = $('#date').val();
		var time = $('#time').val();
		var dateRegex = /^\d{4}-\d{2}-\d{2}$/;
		var timeRegex = /^\d{2}:\d{2}:\d{2}$/;
		if ((street == "") || (district == "")) $("#streetGroup").addClass("has-error");
		else $("#streetGroup").removeClass("has-error");
		if (!dateRegex.test(date)) $("#dateGroup").addClass("has-error");
		else $("#dateGroup").removeClass("has-error");
		if ((time != "") && !timeRegex.test(time)) $("#timeGroup").addClass("has-error");
		else $("#timeGroup").removeClass("has-error");
		if (postId == null) $("#status").html("Ajouter des photos d'abord");
		else if (street == "") $("#status").html("Corrigez la rue");
		else if (district == "") $("#status").html("Corrigez l'arrondissement");
		else if (!dateRegex.test(date)) $("#status").html("Corrigez la date");
		else if ((time != "") && !timeRegex.test(time)) $("#status").html("Corrigez l'heure");
		else {
			$('#report').prop("disabled", true);
			$("#status").html("Mise à jour de la base de données");
			var data;
			if (time == "") data = {'answerCharset': 'UTF-8', 'id': postId, 'number': number, 'street': street, 'district': district, 'date': date};
			else data = {'answerCharset': 'UTF-8', 'id': postId, 'number': number, 'street': street, 'district': district, 'date': date, 'time': time};
			$.ajax({
				url: 'reportUploaded',
				type: 'POST',
				data: data,
				dataType: 'json',
				success: function (json) {
					if (json.result == 'success') {
						$("#uploaded").html("");
						$("#number").val("");
						$("#street").val("");
						street = "";
						district = "";
						$("#date").val("");
						$("#time").val("");
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

