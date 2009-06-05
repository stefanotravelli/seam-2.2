var firstTask = 0;
var maxResults = 25;
var username;


$(document).ready(function() {
	username = $('#username').text();
	
	// next and previous buttons
	$('.next').click(function(event) {
		event.preventDefault();
		firstTask += maxResults;
		loadTasks();
	});
	$('.previous').click(function(event) {
		event.preventDefault();
		firstTask -= maxResults;
		loadTasks();
	});
	
	loadTasks();
});

function loadTasks() {
	$('#tasks tbody').empty();
	getResolvedTasks(username, firstTask, maxResults, function(data) {
		var tasks = $(data).find('task');
		tasks.each(function() {
			addTask($(this));
		})
		// pagination handling
		if (tasks.size() == maxResults) {
			$('.next').show();
		} else { 
			$('.next').hide();
		}
		if (firstTask >= maxResults) {
			$('.previous').show();
		} else {
			$('.previous').hide();
		}
	});
}

function addTask(task) {
	var taskId = $(task).find('id').text();
	var taskName = $(task).find('name').text();
	var categoryName = $(task).find('category').text();
	var taskCompleted = $(task).find('completed').text();
	
	var parent = $('#tasks tbody');
	var nameCell = $('<td/>').addClass('name').text(taskName);
	var completedCell = $('<td/>').addClass('completed').text(taskCompleted);
	var undoButton = $('<img/>').attr('src', 'img/task-undo.png').attr('title', 'Undo this task').click(function(event) {
		event.preventDefault();
		putTask(categoryName, taskId, taskName, false, function() {
			$('#' + taskId).remove();
		});
	});
	var deleteButton = $('<img/>').attr('src', 'img/task-delete.png').attr('title', 'Delete this task').click(function(event) {
		event.preventDefault();
		deleteTask(categoryName, taskId, true, function() {
			$('#' + taskId).remove();
		});
	});
	var buttonCell = $('<td/>').append(undoButton).append(deleteButton);
	$('<tr/>').attr('id', taskId).append(buttonCell).append(nameCell).append(completedCell).appendTo(parent);
}