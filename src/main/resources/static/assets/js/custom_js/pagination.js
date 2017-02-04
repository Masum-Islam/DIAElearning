$(document).ready(function() {
	changePageAndSize();
	
});

function changePageAndSize() {
	$('#pageSizeSelect').change(function(evt) {
		var url = "/teacher/getAssignmentList/?pageSize="+this.value +"&page=1";
		$("#resultsBlock").load(url);
		/*window.location.replace("/teacher/assignments/?pageSize=" + this.value + "&page=1");*/
		/*window.location.href("/teacher/allAssignment/?pageSize="+ this.value + "&page=1");*/
	});
}



function getAssignmentList(pageSize,page,el){
		
	var url = "/teacher/getAssignmentList/?pageSize="+pageSize +"&page="+page;
	jQuery(el).closest('li').addClass("active pointer-disabled").siblings().removeClass("active pointer-disabled");
	$("#resultsBlock").load(url);
	
}