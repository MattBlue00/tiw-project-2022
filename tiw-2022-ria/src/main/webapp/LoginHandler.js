/**
 * Script che gestisce il login di un utente.
 */

(function() { // impedisce alle variabili di divenire globali
	
	this.reset = function(){
		document.getElementById("errorMsgLogin").hidden = true;
		document.getElementById("errorMsgLogin").style.visibility = "hidden";
		document.getElementById("errorMsgRegistration").hidden = true;
		document.getElementById("errorMsgRegistration").style.visibility = "hidden";
		document.getElementById("successMsgRegistration").hidden = true;
		document.getElementById("successMsgRegistration").style.visibility = "hidden";
	}
	
	this.reset();
	
	document.getElementById("loginButton").addEventListener('click', (e) => {  
		
		this.reset();
		
   		var form = e.target.closest("form");   									 
    	if (form.checkValidity()) {
  			makeCall("POST", 'CheckLogin', form,				
     			function(x) {
          			if (x.readyState == XMLHttpRequest.DONE) {
            			var message = x.responseText;									
            			switch (x.status) {
              				case 200:
            					sessionStorage.setItem('username', message);
                				window.location.href = "ImageGallery.html";
                				break;
              				case 400: // bad request
              					document.getElementById("errorMsgLogin").hidden = false;
              					document.getElementById("errorMsgLogin").style.visibility = "visible";
                				document.getElementById("errorMsgLogin").textContent = message;
                				break;
              				case 401: // unauthorized
              					document.getElementById("errorMsgLogin").hidden = false;
              					document.getElementById("errorMsgLogin").style.visibility = "visible";
                  				document.getElementById("errorMsgLogin").textContent = message;
                  				break;
              				case 500: // server error
              					document.getElementById("errorMsgLogin").hidden = false;
              					document.getElementById("errorMsgLogin").style.visibility = "visible";
            					document.getElementById("errorMsgLogin").textContent = message;
                				break;
            			}
          			}
        		}
      		);
    	} else
    	 	form.reportValidity();
  	});
})(); // l'uso delle due parentesi "()" comporta l'invocazione immediata di questa funzione