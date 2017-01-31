/**
 * 
 */

$().ready(function(){   

	  var buttons = $('#assignmentForm input:submit'); 
	  
	  buttons.click(function() {   
	    
		  	var post = $(this).attr("name") + "=" + $(this).val();
		  	
		  	var $btn = $(this);
		  	$btn.button('loading');
		  	
	    	var form_data = $('#assignmentForm').serialize() + "&" + post; 
	    
			$.post($(this).attr("action"), form_data, function(data) {
				
				successMsg(data.message);
				$btn.button('reset');
			}).fail(function(data) {
				  $btn.button('reset');
		        if(data.responseJSON.error.indexOf("InternalError") > -1){
		        	errorMsg(data.responseJSON.message);
		        }else{
		        	var errors = $.parseJSON(data.responseJSON.message);
		            $.each( errors, function( index,item ){
		                errorMsg(item.defaultMessage);
		            });
		        }
		    });
		return false; 
	  });
	  
});  