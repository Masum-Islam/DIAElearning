$(document).ready(function() {

	$(".file-dropzone").on('dragover', handleDragEnter);
	$(".file-dropzone").on('dragleave', handleDragLeave);
	$(".file-dropzone").on('drop', handleDragLeave);

	/*Dropzone.autoDiscover = false;*/
	
	function handleDragEnter(e) {

		this.classList.add('drag-over');
	}

	function handleDragLeave(e) {

		this.classList.remove('drag-over');
	}
	
	

	// "AuthorizationdropzoneForm" is the camel-case version of the form id "dropzone-form"
	Dropzone.options.myAwesomeDropzone  = {

		url : "upload",
		acceptedFiles: ".jpeg,.jpg",
		autoProcessQueue : false,
		/*uploadMultiple : true,*/
		maxFilesize : 0.1, // MB
		/*parallelUploads : 100,*/
		maxFiles : 1,
		maxfilesexceeded: function(file) {
	        this.removeAllFiles();
	        this.addFile(file);
	    },
		addRemoveLinks : true,
		previewsContainer : ".dropzone-previews",

		// The setting up of the dropzone
		init : function() {

			var myDropzone = this;

			// first set autoProcessQueue = false
			$('#upload-button').on("click", function(e) {

				myDropzone.processQueue();
				
			});

			// customizing the default progress bar
			this.on("uploadprogress", function(file, progress) {

				progress = parseFloat(progress).toFixed(0);

				var progressBar = file.previewElement.getElementsByClassName("dz-upload")[0];
				progressBar.innerHTML = progress + "%";
				
			});
			
			this.on("error", function(file, response) {
                // do stuff here.
                /*alert(response);*/
                errorMsg(response);
            });
			
			// displaying the uploaded files information in a Bootstrap dialog
			this.on("success", function(files, serverResponse) {
				successMsg("User Photo Upload Successfully");
				/*showInformationDialog(files, serverResponse);*/
			});
			
			this.on("complete", function(file) {
				this.removeFile(file);
			});
			
		}
	}
	

	function showInformationDialog(files, objectArray) {

		var responseContent = "";

		for (var i = 0; i < objectArray.length; i++) {

			var infoObject = objectArray[i];

			for ( var infoKey in infoObject) {
				if (infoObject.hasOwnProperty(infoKey)) {
					responseContent = responseContent + " " + infoKey + " -> " + infoObject[infoKey] + "<br>";
				}
			}
			responseContent = responseContent + "<hr>";
		}

		// from the library bootstrap-dialog.min.js
		BootstrapDialog.show({title : '<b>Server Response</b>',message : responseContent});
	}
	
	function successMsg(msg){
		
		$.notify(msg, {
			
			type: "success",
			delay: 5000,
			animate: {
				enter: 'animated fadeInRight',
				exit: 'animated fadeOutRight'
			},
			newest_on_top: true
		});
	}
	
	
	function errorMsg(msg){
		
		$.notify(msg, {
			type: "danger",
			delay: 5000,
			animate: {
				enter: 'animated fadeInRight',
				exit: 'animated fadeOutRight'
			},
			newest_on_top: true
		});
		
	}
	

});