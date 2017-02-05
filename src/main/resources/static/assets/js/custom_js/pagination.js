$(document).ready(function() {
	changePageAndSize();
	
});

function changePageAndSize() {
	$('#pageSizeSelect').change(function(evt) {
		/*var url = "/teacher/getAssignmentList/?pageSize="+this.value +"&page=1";
		$("#resultsBlock").load(url);*/
		window.location.replace("/teacher/assignments/?pageSize=" + this.value + "&page=1");
		/*window.location.href("/teacher/allAssignment/?pageSize="+ this.value + "&page=1");*/
	});
}

/*$('#searchAssignment').on("keypress", function(e) {
    if (e.keyCode == 13) {
        
        alert("Enter search:"+search);
        var formData= $('searchForm').serialize();
        var url = "/teacher/assignments/search";
    	$.get(url+formData,function(data) {
    		$("#resultsBlock").load(url);
        });
    	
        return false; // prevent the button click from happening
    }
});
*/



/*function getAssignmentList(pageSize,page,el){
		
	var url = "/teacher/getAssignmentList/?pageSize="+pageSize +"&page="+page;
	jQuery(el).closest('li').addClass("active pointer-disabled").siblings().removeClass("active pointer-disabled");
	$("#resultsBlock").load(url);
	
}*/


$('.btn-filter').on('click', function () {
    var $target = $(this).data('target');
    if ($target != 'all') {
      $('.table tr').css('display', 'none');
      $('.table tr[data-status="' + $target + '"]').fadeIn('slow');
    } else {
      $('.table tr').css('display', 'none').fadeIn('slow');
    }
});
