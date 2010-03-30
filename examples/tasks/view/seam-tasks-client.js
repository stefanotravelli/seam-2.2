$(document).ready(function() {
	$.ajaxSetup({ cache: false }); // workaround for IE
})

function getCategories(callback) {
	$.get("seam/resource/v1/auth/category", callback);
}

function putCategory(categoryName, callback) {
	$.ajax({
		type: "PUT",
		url: "seam/resource/v1/auth/category/" + categoryName,
		contentType: "application/xml",
		success: callback
	});
}

function deleteCategory(categoryName, callback) {
	$.ajax({
		type: "DELETE",
		url: "seam/resource/v1/auth/category/" + categoryName,
		success: callback
	});
}

function getTask(categoryName, taskId, taskDone, callback) {
	var URI = escape("seam/resource/v1/auth/category/" + categoryName + (taskDone ? "/resolved" : "/unresolved"));
	$.get(URI, callback);
}

function getTasksForCategory(categoryName, taskDone, callback) {
	var URI = escape("seam/resource/v1/auth/category/" + categoryName + (taskDone ? "/resolved" : "/unresolved"));
	$.get(URI, function(data) {
		callback(data);
	});
}

function getResolvedTasks(username, start, show, callback) {
	var URI = escape("seam/resource/v1/user/" + username + "/tasks/resolved.xml");
	URI += "?start=" + start + "&show=" + show;
	$.get(URI, function(data) {
		callback(data);
	});
}

function postTask(categoryName, taskName, callback) {
	var URI = escape("seam/resource/v1/auth/category/" + categoryName + "/unresolved");
	var data = '<task><name>' + taskName + '</name></task>';
	var request = $.ajax({
		type: "POST",
		url: URI,
		contentType: "application/xml",
		data: data,
		success: function() {
			callback(request.getResponseHeader('Location'));
		}
	});
}

function putTask(categoryName, taskId, taskName, taskDone, callback) {
	var URI = escape("seam/resource/v1/auth/category/" + categoryName + (taskDone ? "/resolved/" : "/unresolved/") + taskId);
	var data = '<task><id>' + taskId + '</id><name>' + taskName + '</name></task>'
	$.ajax({
		type: "PUT",
		url: URI,
		contentType: "application/xml",
		data: data,
		success: function() {
			callback(data);
		}
	});
}

function deleteTask(categoryName, taskId, taskDone, callback) {
	var URI = escape("seam/resource/v1/auth/category/" + categoryName + (taskDone ? "/resolved/" : "/unresolved/") + taskId);
	$.ajax({
		type: "DELETE",
		url: URI,
		success: callback
	});
}