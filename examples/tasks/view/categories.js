$(document).ready(function() {
	// load category list
	getCategories(function(data) {
		$(data).find('category').each(function() {
			var categoryName = $(this).find('name').text();
			addCategory(categoryName);
	 	});
	});
	// create new category on submit
	$('#editCategorySubmit').click(function(event) {
		event.preventDefault();
		var categoryName = $('#editCategoryName').attr('value');
		putCategory(categoryName, function() {
			addCategory(categoryName);
			$('#editCategoryName').attr('value', '');
		});
	});
});

function addCategory(categoryName) {
	var nameCell = $('<td/>').addClass('name').text(categoryName);
	var deleteButton = $('<img/>').attr('src', 'img/task-delete.png').attr('title', 'Delete this category').click(function(event) {
		event.preventDefault();
		deleteCategory(categoryName, function() {
			$('[id=' + categoryName + ']').remove();
		});
	});
	var buttonCell = $('<td/>').append(deleteButton);
	$('<tr/>').attr('id', categoryName).append(buttonCell).append(nameCell).appendTo('#categories tbody');
}