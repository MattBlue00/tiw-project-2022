/**
 * Script che gestisce la registrazione di un utente.
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

  	document.getElementById("registrationButton").addEventListener('click', (e) => {
	
		this.reset();
	
	    var form = e.target.closest("form");
	    var email = document.getElementById("email").value;
	    var password = document.getElementById("password").value;
	    var passwordRepeated = document.getElementById("passwordRepeated").value;
	    var validEmailRegex = /^([a-zA-Z0-9_.-])+@(([a-zA-Z0-9-]{2,})+.)+([a-zA-Z0-9]{2,})+$/;
	  
	  	// se password===passwordRepeated e l'email inserita è valida, viene invocata la CheckRegister
	    if (form.checkValidity() && password===passwordRepeated && validEmailRegex.test(email)) {
	      	makeCall("POST", 'CheckRegister', form,
	        	function(x) {
	          		if (x.readyState == XMLHttpRequest.DONE) {
	            		var message = x.responseText;
	            		switch (x.status) {
	              			case 200: // OK
	              				document.getElementById("successMsgRegistration").hidden = false;
								document.getElementById("successMsgRegistration").style.visibility = "visible";
	              				document.getElementById("successMsgRegistration").textContent = message;
	                			break;
	              			case 400: // bad request
	              				document.getElementById("errorMsgRegistration").hidden = false;
								document.getElementById("errorMsgRegistration").style.visibility = "visible";
                				document.getElementById("errorMsgRegistration").textContent = message;
	                			break;
	              			case 500: // server error
	              				document.getElementById("errorMsgRegistration").hidden = false;
								document.getElementById("errorMsgRegistration").style.visibility = "visible";
	            				document.getElementById("errorMsgRegistration").textContent = message;
	                			break;
	            		}
	          		}
	        	}
	    	);
	    } else {
			if (password!==passwordRepeated){
				document.getElementById("errorMsgRegistration").hidden = false;
				document.getElementById("errorMsgRegistration").style.visibility = "visible";
				document.getElementById("errorMsgRegistration").textContent = "I campi password e ripeti password devono essere uguali.";
			}
			else if(!validEmailRegex.test(email)){
				document.getElementById("errorMsgRegistration").hidden = false;
				document.getElementById("errorMsgRegistration").style.visibility = "visible";
				document.getElementById("errorMsgRegistration").textContent = "L'email inserita non è valida.";
			}
			else{
				document.getElementById("errorMsgRegistration").hidden = false;
				document.getElementById("errorMsgRegistration").style.visibility = "visible";
				document.getElementById("errorMsgRegistration").textContent = "";
				form.reportValidity();
			}
	    }
	});
})(); // l'uso delle due parentesi "()" comporta l'invocazione immediata di questa funzione