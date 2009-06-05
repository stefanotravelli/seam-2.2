$(document).ready(function() {
	printCategories();
	$('#editTaskSubmit').click(function() {
		var name = $('#editTaskName').val();
		var categoryName = $('#editTaskCategory').val()
		postTask(categoryName, name, function(location) {
			$('#editTaskName').val('');
			$.get(location, function(data) {
				addTask($(data).find('task'), categoryName);
			});
		});
	});
});

function showTaskEditForm(categoryName, taskId) {
	var categories = $('#editTaskCategory').clone();
	$(categories).find('[value=' + categoryName + ']').attr('selected', 'selected');
	var taskName = $('#' + taskId + " .name").text();
	var name = $('<input/>').attr('type', 'text').addClass('nameField').val(taskName);
	
	var update = $('<input/>').attr('type', 'button').attr('id', 'update').val('Update').click(function() {
		var newCategoryName = $(categories).val();
		var callback = function(data) {
			removeTaskEditForm(taskId, taskName);
				if (categoryName == newCategoryName) {
					updateTaskNameOnUI(taskId, data); // just update the name
				} else {
					$('#' + taskId).remove(); // add the task into new category
					addTask(data, newCategoryName);
				}
		}
		putTask(newCategoryName, taskId, $(name).val(), false, callback);
	});
	var form = $('<form/>').attr('id', 'updateTask').append(categories).append(name).append(update);
	$('#' + taskId + ' .name').replaceWith(form);
}

function removeTaskEditForm(taskId, taskName) {
	$('#' + taskId + ' form').replaceWith($('<span/>').addClass('name').text(taskName));
}

function printCategories() {
	getCategories(function(data) {
 	   $(data).find('category').each(function() {
 		   addCategory($(this));
 	   });
	});
}

function addCategory(category) {
	var categoryName = $(category).find('name').text();
	var escapedCategoryName = escape(categoryName);
	var categoryCell = $('<td/>').attr('colspan', '2').addClass('name').text(categoryName);
	var categoryRow = $('<tr/>').attr('id', categoryName).append(categoryCell);
	$('#categories tbody').append(categoryRow).appendTo('#categories');
	$('<option/>').attr('value', categoryName).text(categoryName).appendTo('#editTaskCategory');
	getTasksForCategory(categoryName, false, function(data) {
		$(data).find('task').each(function() {
			addTask($(this), categoryName);
	 	});
	});
}

function addTask(task, categoryName) {
	var taskId = $(task).find('id').text();
	var taskName = $(task).find('name').text();
	
	var parent = $('[id=' + categoryName + ']');
	var nameCell = $('<td/>').append($('<span/>').addClass('name').text(taskName));
	var doneButton = $('<img/>').attr('src', 'img/task-done.png').attr('title', 'Resolve this task').click(function(event) {
		event.preventDefault();
		putTask(categoryName, taskId, taskName, true, function() {
			$('#' + taskId).remove();
		});
	});
	var editButton = $('<img/>').attr('src', 'img/task-edit.png').attr('title', 'Edit this task').click(function(event) {
		event.preventDefault();
		if ($('#' + taskId + ' #updateTask').size() == 0) {
			showTaskEditForm(categoryName, taskId);
		} else {
			removeTaskEditForm(taskId, taskName);
		}
	});
	var deleteButton = $('<img/>').attr('src', 'img/task-delete.png').attr('title', 'Delete this task').click(function(event) {
		event.preventDefault();
		deleteTask(categoryName, taskId, false, function() {
			$('#' + taskId).remove();
		});
	});
	var buttonCell = $('<td/>').append(doneButton).append(editButton).append(deleteButton);
	$('<tr/>').attr('id', taskId).append(buttonCell).append(nameCell).insertAfter('[id=' + categoryName + ']');
}

function updateTaskNameOnUI(taskId, task) {
	var taskName = $(task).find('name').text();
	$('#' + taskId + " .name").text(taskName);
}
