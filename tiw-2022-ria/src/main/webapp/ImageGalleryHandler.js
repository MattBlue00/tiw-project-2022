/**
 * Script della pagina principale dell'applicazione.
 */
 
 {
	
	/**
	 * pageOrchestrator gestisce l'intera pagina, che è formata da diversi componenti.
	 */
	 
	 let pageOrchestrator = new PageOrchestrator();
	
	/**
	 * Componenti della pagina gestiti da pageOrchestrator:
	 * - personalMessage gestisce il messaggio di benvenuto personalizzato.
	 * - myAlbums gestisce la lista degli album dell'utente.
	 * - otherAlbums gestisce la lista degli album degli altri utenti.
	 * - albumDetails gestisce la visualizzazione dei dettagli dell'album, compresa la finestra modale.
	 * - createComment gestisce la form per l'inserimento di un commento.
	 * - comments gestisce la lista di commenti riguardanti la foto selezionata (se presente).
	 */
	 
	let personalMessage, myAlbums, otherAlbums, albumDetails,
	createAlbum, createComment, comments;
	
	/**
	 * myAlbumsOrder permette la gestione di un ordine degli album propri dell'utente
	 * personalizzato dall'utente stesso. L'ordine viene definito grazie alla funzione di 
	 * drag and drop.
	 */
	
	let myAlbumsOrder;
	
	/**
	 * Si controlla che l'utente sia effettivamente in sessione. Qualora non fosse così,
	 * si rimanda alla pagina di login.
	 */
	
	window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null) {
	      window.location.href = "login.html";
	    } else {
	      pageOrchestrator.start(); // l'utente è loggato: il pageOrchestrator può partire
	      pageOrchestrator.refresh();
	    }
	}, false);	
	  
	/**
	 * Gestisce l'intera pagina dell'applicazione web.
	 */
	
	function PageOrchestrator() {
		
		// contenitori per i messaggi di allerta
		var alert = document.getElementById("genericAlert");
		
		/**
		 * Inizializza tutti i componenti della pagina.
		 */
		
	    this.start = function() { 
		
			document.getElementById("genericAlert").style.display = "none";
		
	    	personalMessage = new PersonalMessage(sessionStorage.getItem('username'), document.getElementById("personalMessage"));
	      	personalMessage.show();
						
			myAlbums = new MyAlbums(alert, document.getElementById("myAlbumsContainer"), document.getElementById("myAlbumsBody"));	
			
			myAlbumsOrder = new MyAlbumsOrder();	
			myAlbumsOrder.registerSaveOrderEvent();
			myAlbumsOrder.registerDefaultOrderEvent();
			
			createAlbum = new CreateAlbum(alert, document.getElementById("addImageContainer"), this);
			createAlbum.registerAlbumCreationEvent();
			createAlbum.registerAddImageEvent();
			createAlbum.registerFinishAlbumEvent();
			
			otherAlbums = new OtherAlbums(alert, document.getElementById("otherAlbumsContainer"), document.getElementById("otherAlbumsBody"));
			
			albumDetails = new AlbumDetails(alert, document.getElementById("imageThumbnailsContainer"),document.getElementById("imageThumbnailsBody"));
	     	albumDetails.registerPreviousNextEvent();
	     	
	     	comments = new Comments(alert, document.getElementById("commentsContainer"), document.getElementById("commentsBody"));
	     	
	     	createComment = new CreateComment();					 
			createComment.registerAddCommentEvent();
		
			// se l'utente schiaccia su "Logout", rimuove dalla sessione il suo username
	    	document.querySelector("a[href='Logout']").addEventListener('click', () => {
				makeCall("GET", 'Logout', null,
     				function(x) {
          				if (x.readyState == XMLHttpRequest.DONE) {									
            				switch (x.status) {
              					case 200:
					            	window.sessionStorage.removeItem('username');
					                window.location.href = "login.html";
                					break;
             					case 404:
             						document.getElementById("genericAlert").style.display = "inline";
            						document.getElementById("genericAlert").textContent = "Impossibile uscire dall'applicazione, riprova.";
              						break;    
            				}
          				}
        			}
      			);
	      	})
	    };
	    
	    /**
	     * Ricarica la pagina come se fosse riacceduta per la prima volta (ma con i dati
	     * eventualmente aggiornati).
	     */
	    
	    this.refresh = function() {
			albumDetails.reset();
			myAlbums.reset();
			myAlbums.show();
			myAlbumsOrder.reset()
			createAlbum.reset();
			otherAlbums.reset();
			otherAlbums.show();
			document.getElementById("genericAlert").style.display = "none";
	    };
	    
	}
	
	/**
	 * Gestisce il messaggio di benvenuto personalizzato.
	 */

	function PersonalMessage(_username, _messageContainer) {
		
	    this.username = _username;
	    this.messageContainer = _messageContainer;
	    
	    /**
	     * Mostra il mesaggio di benvenuto personalizzato.
	     */
	    
	    this.show = function() {
	      this.messageContainer.textContent = this.username; 
	    }
	}
	
	/**
	 * Gestisce la lista degli album relativi all'utente.
	 */
	
	function MyAlbums(_alert, _myAlbumsContainer, _myAlbumsBody){ 
		
		this.alert = _alert;
		this.myAlbumsContainer=_myAlbumsContainer;
		this.myAlbumsBody=_myAlbumsBody;
		
		/**
		 * Nasconde la tabella contenente le informazioni degli album.
		 */
		 
		this.reset = function() { 
	    	this.myAlbumsContainer.style.display = "none";
	    };
	    
	    /**
	     * Mostra la tabella contenente le informazioni degli album secondo l'ordine
	     * personalizzato, se presente. In caso non fosse presente, viene utilizzato
	     * l'ordine di default (data di creazione decrescente).
	     */
	    
	    this.show = function() {
			var self = this;
			self.alert.style.display = "none";
			document.getElementById("viewMyAlbumsText").style.display = "inline";
			document.getElementById("saveCustomOrderButton").style.display = "inline";
			document.getElementById("showDefaultOrderButton").style.display = "inline";
			this.myAlbumsContainer.style.display = "inline";
	      	makeCall("GET", "GetUserAlbums", null,
	        	function(req) {
	          		if (req.readyState == 4) {
	            		var message = req.responseText;
	            		if (req.status == 200) {
	              			var albumsToShow = JSON.parse(req.responseText);
	              			if (albumsToShow.length == 0) {
								self.alert.style.display = "inline";
	                			self.alert.textContent = "Non hai ancora caricato degli album.";
	                			self.myAlbumsContainer.style.display = "none";
	                			document.getElementById("viewMyAlbumsText").style.display = "none";
	                			document.getElementById("saveCustomOrderButton").style.display = "none";
								document.getElementById("showDefaultOrderButton").style.display = "none";
	                			return;
	              			}
	              			self.update(albumsToShow); // self visible by closure
	          			} else if (req.status == 403) {
                  			window.location.href = req.getResponseHeader("Location");
                  			window.sessionStorage.removeItem('username');
                  		}
                  		else {
							if(message != null){
								self.alert.style.display = "inline";
	            				self.alert.textContent = message;
	            				self.myAlbumsContainer.style.display = "none";
            				}
	          			}
	          		}
	        	}
	      ); 
	    };
	    
	    /**
	     * Mostra la tabella contenente le informazioni degli album secondo l'ordine
	     * di default (data di creazione decrescente).
	     */
	    
	    this.showOrderByData = function() {
			var self = this;
			self.alert.style.display = "none";
			this.myAlbumsContainer.style.display = "inline";
	      	makeCall("GET", "GetUserAlbumsByDate", null,
	        	function(req) {
	          		if (req.readyState == 4) {
	            		var message = req.responseText;
	            		if (req.status == 200) {
	              			var albumsToShow = JSON.parse(req.responseText);
	              			if (albumsToShow.length == 0) {
								self.alert.style.display = "inline";
	                			self.alert.textContent = "Non hai ancora caricato degli album.";
	                			self.myAlbumsContainer.style.display = "none";
	                			return;
	              			}
	              			self.update(albumsToShow); // self visible by closure
	          			} else if (req.status == 403) {
                  			window.location.href = req.getResponseHeader("Location");
                  			window.sessionStorage.removeItem('username');
                  		}
                  		else {
	           				if(message != null){
								self.alert.style.display = "inline";
	            				self.alert.textContent = message;
	            				self.myAlbumsContainer.style.display = "none";
            				}
	          			}
          			}
	        	}
	      	); 
		};
	    
	    /**
	     * Carica dinamicamente la lista degli album correntemente presenti nel database.
	     * Inoltre, rende ogni riga contenente le informazioni di un album "draggable",
	     * così da permettere all'utente di modificare la lista a proprio piacimento.
	     */
	    
	   	this.update = function(arrayAlbums) {  
		
	    	var row, datacell, anchor;
	      	this.myAlbumsBody.innerHTML = ""; // svuota il corpo della tabella
	      	var self = this;
	      	
	      	arrayAlbums.forEach(function(album) { // self visible here, not this
	      	
	        	row = document.createElement("tr");
	        	row.draggable = true;
	        	row.addEventListener("dragstart", (e)=>{dragStart(e);});
	        	row.addEventListener("dragleave", (e)=>{dragLeave(e);});
	        	row.addEventListener("dragover", (e)=>{dragOver(e);});
	        	row.addEventListener("drop", (e)=>{drop(e);});
	        	
	        	linkcell = document.createElement("td");
	        	anchor = document.createElement("a");
	        	linkcell.appendChild(anchor);
	        	linkText = document.createTextNode(album.title);
	        	anchor.appendChild(linkText);
		        anchor.setAttribute('albumTitle', album.title); // imposta un attributo HTML custom
		        anchor.setAttribute('albumOwner', album.owner); // imposta un attributo HTML custom
		        anchor.addEventListener("click", (e) => {
					createAlbum.reset();
		          	albumDetails.reset();
		          	albumDetails.showImages(e.target.getAttribute("albumTitle"), e.target.getAttribute("albumOwner"));
		          	document.getElementById("addImageContainer").style.display = "inline";
		          	document.getElementById("addImageHeader").style.display = "inline";
		          	document.getElementById("addImageHeader").textContent = "Aggiungi immagini all'album \"" + e.target.getAttribute("albumTitle") + "\"";
		          	window.sessionStorage.setItem("albumToModify", e.target.getAttribute("albumTitle"));
		        }, false);
		        anchor.href = "#";
		        row.appendChild(linkcell);
		        datacell = document.createElement("td");
		        datacell.textContent = album.creationDate;
		        row.appendChild(datacell);
		        self.myAlbumsBody.appendChild(row);
		        
	      });
		  
	    };
		
	}
	
	/**
	 * Gestisce l'ordinamento personalizzabile degli album dell'utente.
	 */
	
	function MyAlbumsOrder(){
		
		/**
		 * Resetta i messaggi di successo/errore relativi all'ordinamento degli album.
		 */
		
		this.reset = function(){
			document.getElementById("saveOrderError").hidden = true;
			document.getElementById("saveOrderError").style.visibility = "hidden";
			document.getElementById("saveOrderSuccess").hidden = true;
			document.getElementById("saveOrderSuccess").style.visibility = "hidden";
		}
		
		/**
		 * Salva il nuovo ordine degli album imposto dall'utente grazie al drag and drop.
		 */
		
		this.registerSaveOrderEvent = function(){
			
			document.getElementById("saveCustomOrderButton").addEventListener('click', (e)=>{
				
				this.reset();
				
				// crea una lista contenente il nuovo ordine degli album
				var lista = [];
				for(let i=0; i<document.getElementById("myAlbumsBody").childElementCount ; i++){
					 lista[i] = document.getElementById("myAlbumsBody").children[i].children[0].textContent;
				}
				
				// crea un campo di form fittizio con cui allegare la lista alla richiesta
				var form = e.target.closest("form");
				var input = document.createElement("input");
				input.style.display = "none";
				input.setAttribute("type", "text");
				input.setAttribute("name", "lista");
				input.setAttribute("value", lista);
				form.appendChild(input);
				
				if (form.checkValidity()) {
					makeCall("POST", "SaveOrder", form,
				        function(x) {
				          	if (x.readyState == XMLHttpRequest.DONE) {                        
					            var message = x.responseText;									
					            switch (x.status) {
									case 200:
				                  		document.getElementById("saveOrderSuccess").hidden = false;
										document.getElementById("saveOrderSuccess").style.visibility = "visible";
										document.getElementById("saveOrderSuccess").textContent = message;
				                  		break;
			                  		case 403:
				                   		window.location.href = req.getResponseHeader("Location");
				                    	window.sessionStorage.removeItem('username');
				                    	break;
			                  		case 400:
				                  	case 500:	
				                  		document.getElementById("saveOrderError").hidden = false;
										document.getElementById("saveOrderError").style.visibility = "visible";
										document.getElementById("saveOrderError").textContent = message;
				                  		break;
					            }
				        	}
						}
					);
			    }
		      	else{
					form.reportValidity();
				}
				form.removeChild(input);
			})
		};
		
		/**
		 * Mostra gli album ordinati con il criterio di default (data di creazione decrescente).
		 */
		
		this.registerDefaultOrderEvent = function(){
			document.getElementById("showDefaultOrderButton").addEventListener('click', (e)=>{
				this.reset();
				myAlbums.reset();
				myAlbums.showOrderByData();
			})
		}

	}
	
	/**
	 * Gestisce la creazione di un album.
	 */
	
	function CreateAlbum(_alert, _addImageContainer, _pageOrchestrator){
		
		this.alert = _alert;
		this.addImageContainer = _addImageContainer;
		this.pageOrchestrator = _pageOrchestrator;
		
		/**
		 * Fa scomparire i messaggi di successo e di errore che riguardano la creazione di
		 * un album.
		 */
		
		this.reset = function(){
      		document.getElementById("addImageHeader").style.display = "none";
			this.addImageContainer.style.display = "none";
			document.getElementById("createAlbumSuccess").hidden = true;
			document.getElementById("createAlbumSuccess").style.visibility = "hidden";
			document.getElementById("createAlbumError").hidden = true;
			document.getElementById("createAlbumError").style.visibility = "hidden";
			document.getElementById("addImageSuccess").hidden = true;
			document.getElementById("addImageSuccess").style.visibility = "hidden";
			document.getElementById("addImageError").hidden = true;
			document.getElementById("addImageError").style.visibility = "hidden";
		};
		
		/**
		 * Permette la creazione di un album. Una volta creato, causa l'apparizione di una
		 * form che permette l'aggiunta di immagini in quello stesso album, se lo si desidera.
		 */
		
		this.registerAlbumCreationEvent = function(){
			
			document.getElementById("createAlbumButton").addEventListener('click', (e)=>{
				
				var form = e.target.closest("form");
				window.sessionStorage.setItem("albumToModify", document.getElementById("newAlbumTitle").value);
				
				this.reset();

				if (form.checkValidity()){
					makeCall("POST", "CreateAlbum", form,
						function(x) {
				         	if (x.readyState == XMLHttpRequest.DONE) {    
				            	var message = x.responseText;									
					            switch (x.status) {
									case 200:
										document.getElementById("createAlbumSuccess").hidden = false;
										document.getElementById("createAlbumSuccess").style.visibility = "visible";
				                  		document.getElementById("createAlbumSuccess").textContent = message;
				                  		document.getElementById("addImageContainer").style.display = "inline";
				                  		document.getElementById("addImageHeader").style.display = "inline";
		          						document.getElementById("addImageHeader").textContent = "Aggiungi immagini all'album \"" + window.sessionStorage.getItem("albumToModify") + "\"";
		          						myAlbums.reset();
		          						myAlbums.show();
				                  		break;
			                  		case 403:
				                   		window.location.href = req.getResponseHeader("Location");
				                    	window.sessionStorage.removeItem('username');
				                    	break;
			                  		case 400:
				                  	case 500:
				                  		document.getElementById("createAlbumError").hidden = false;
										document.getElementById("createAlbumError").style.visibility = "visible";
										document.getElementById("createAlbumError").textContent = message;
				                  		break;
				            	}
				        	} else {
		    	 				form.reportValidity(); 
		    				}  
		    			}
	    			);	
    			}
    			
			});
		};
		
		/**
		 * Permette l'upload di un'immagine e l'inserimento di un titolo e una descrizione per
		 * essa, mediante un'opportuna form.
		 */
		
		this.registerAddImageEvent = function(){
			
			document.getElementById("insertIntoAlbumButton").addEventListener('click', (e)=>{
				
				var form = e.target.closest("form");
				var self = this;
				
				var albumTitle = document.createElement("input");
				albumTitle.style.display = "none";
				albumTitle.setAttribute("type", "text");
				albumTitle.setAttribute("name", "albumToModify");
				albumTitle.setAttribute("value", window.sessionStorage.getItem("albumToModify"));
				form.appendChild(albumTitle);
				
				if (form.checkValidity()){
					makeCall("POST", "AddImage", form,
						function(x) {
				          	if (x.readyState == XMLHttpRequest.DONE) {    
				            var message = x.responseText;
				            self.reset();
				            self.addImageContainer.style.display = "inline";	
							document.getElementById("addImageHeader").style.display = "inline";
							document.getElementById("addImageHeader").textContent = "Aggiungi immagini all'album \"" + window.sessionStorage.getItem("albumToModify") + "\"";				
					            switch (x.status) {
									case 200:
										document.getElementById("addImageSuccess").hidden = false;
										document.getElementById("addImageSuccess").style.visibility = "visible";
				                  		document.getElementById("addImageSuccess").textContent = message;
				                  		albumDetails.reset();
				                  		albumDetails.showImages(window.sessionStorage.getItem("albumToModify"), window.sessionStorage.getItem("username"));
				                  		break;
			                  		case 403:
				                   		window.location.href = req.getResponseHeader("Location");
				                    	window.sessionStorage.removeItem('username');
				                    	break;
			                  		case 400:
				                  	case 500:
				                  		document.getElementById("addImageError").hidden = false;
										document.getElementById("addImageError").style.visibility = "visible";
				                  		document.getElementById("addImageError").textContent = message;
				                  		break;
					            }
					        }
			    		}
	    			);
				}
				else {
    	 			form.reportValidity();
    			}
    			form.removeChild(albumTitle);
			});
		};
		
		/**
		 * Permette di terminare il processo di upload di immagini in un album. Di conseguenza,
		 * nasconde la form e aggiorna la pagina con il nuovo album.
		 */
		
		this.registerFinishAlbumEvent = function(){
			document.getElementById("finishAlbumButton").addEventListener('click', (e)=>{
				this.reset();
				this.pageOrchestrator.refresh();
			});
		};
	}
	
	/**
	 * Gestisce la lista degli album relativi agli altri utenti.
	 */
	
	function OtherAlbums(_alert, _otherAlbumsContainer, _otherAlbumsBody){ 
		
		this.alert = _alert;
		this.otherAlbumsContainer=_otherAlbumsContainer;
		this.otherAlbumsBody=_otherAlbumsBody;
		
		/**
		 * Nasconde la tabella contenente le informazioni degli album.
		 */
		 
		this.reset = function() {  
	      this.otherAlbumsContainer.style.display = "none";
	    };
	    
	    /**
	     * Mostra la tabella contenente le informazioni degli album secondo l'ordine
	     * di default (data di creazione decrescente).
	     */
	    
	    this.show = function() {
			var self = this;
			self.alert.style.display = "none";
			this.otherAlbumsContainer.style.display = "inline";
	      	makeCall("GET", "GetOtherAlbums", null,
	        	function(req) {
	          		if (req.readyState == 4) {
	            		var message = req.responseText;
	            		if (req.status == 200) {
	              			var albumsToShow = JSON.parse(req.responseText);
	              			if (albumsToShow.length == 0) {
								self.alert.style.display = "inline";
	                			self.alert.textContent = "Non ci sono album di altri da mostrare.";
	                			self.otherAlbumsContainer.style.display = "none";
	                			return;
	              			}
	             			self.update(albumsToShow); // self visible by closure
		          		} else if (req.status == 403) {
			                  window.location.href = req.getResponseHeader("Location");
			                  window.sessionStorage.removeItem('username');
						} else {
		            		if(message != null){
								self.alert.style.display = "inline";
	            				self.alert.textContent = message;
	            				self.otherAlbumsContainer.style.display = "none";
            				}
	          			}
          			}
	        	}
	      	);
	    };
	    
	    /**
	     * Carica dinamicamente la lista degli album correntemente presenti nel database.
	     */
	    
	    this.update = function(arrayAlbums) {
		  	
	      	var row, idcell, datacell, anchor;
	      	this.otherAlbumsBody.innerHTML = ""; // svuota il corpo della tabella
	      	
	      	var self = this;
	      	arrayAlbums.forEach(function(album) { // self visible here, not this
	      	
		        row = document.createElement("tr");
		        linkcell = document.createElement("td");
		        anchor = document.createElement("a");
		        linkcell.appendChild(anchor);
		        linkText = document.createTextNode(album.title);
		        anchor.appendChild(linkText);
		        anchor.setAttribute('albumTitle', album.title); // imposta un attributo HTML custom
		        anchor.setAttribute('albumOwner', album.owner);
		        anchor.addEventListener("click", (e) => {
					createAlbum.reset();
		          	albumDetails.reset();
		          	albumDetails.showImages(e.target.getAttribute("albumTitle"), e.target.getAttribute("albumOwner"));
		    	}, false);
	        anchor.href = "#";
	        row.appendChild(linkcell);
	        idcell = document.createElement("td");
	        idcell.textContent = album.owner;
	        row.appendChild(idcell);
	        datacell = document.createElement("td");
	        datacell.textContent = album.creationDate;
	        row.appendChild(datacell);
	        self.otherAlbumsBody.appendChild(row);
	        
	      });
	      
	      this.otherAlbumsContainer.style.visibility = "visible";

	    };
	    
	}
	
	/**
	 * Gestisce il caricamento delle cinque miniature da visionare dell'album scelto.
	 */
	
	function AlbumDetails(_alert, _imageThumbnailsContainer, _imageThumbnailsBody){ 
		
		this.alert = _alert;
		this.imageThumbnailsContainer = _imageThumbnailsContainer;
		this.imageThumbnailsBody = _imageThumbnailsBody;
		let imagesToShow;
		let range;
		
		/**
		 * Resetta il contenitore delle cinque miniature da mostrare.
		 */

		this.reset = function(){
			range = 0;
			document.getElementById("imageThumbnailsFieldset").hidden = true;
			this.imageThumbnailsContainer.style.display = "none";
			document.getElementById("nextButton").style.visibility = "hidden";
			document.getElementById("previousButton").style.visibility = "hidden";
			document.getElementById("albumDetailsHeader").style.display = "none";
			document.getElementById("noImages").style.display = "none";
			document.getElementById("modal").style.display = "none";
			document.getElementById("commentsContainer").style.display = "none";
		};
		
		/**
		 * Mostra le cinque precedenti/successive immagini dell'album, a seconda del pulsante
		 * scelto.
		 */
		
		this.registerPreviousNextEvent = function(){
			
			document.getElementById("nextButton").addEventListener('click', (e) => {
				range += 1;
				this.updateImages(imagesToShow);
			});
			document.getElementById("previousButton").addEventListener('click', (e) => {
				range -= 1;
				this.updateImages(imagesToShow);
			});
	     	
		};
		
		/**
		 * Ottiene le immagini dell'album selezionato.
		 */
		
		this.showImages = function(albumTitle, albumOwner){ 
			
			var self = this;
			self.alert.style.display = "none";
			document.getElementById("imageThumbnailsFieldset").hidden = false;
			this.imageThumbnailsContainer.style.display = "inline";
			document.getElementById("albumDetailsHeader").style.display = "inline";
			document.getElementById("albumDetailsHeader").textContent = "Visualizza l'album \"" + albumTitle +"\" di " + albumOwner;
			
      		makeCall("GET", "GetImages?albumTitle="+albumTitle+"&albumOwner="+albumOwner, null,
        		function(req) { 
      				if (req.readyState == 4) {
		 				var message = req.responseText;
        				if (req.status == 200) {
          					imagesToShow = JSON.parse(message);
          					self.updateImages(imagesToShow); // self visible by closure
      					} else if (req.status == 403) {
			            	window.location.href = req.getResponseHeader("Location");
			                window.sessionStorage.removeItem('username');
        				} else if(req.status == 500){
							if(message != null){
								self.alert.style.display = "inline";
	            				self.alert.textContent = message;
	            				document.getElementById("imageThumbnailsFieldset").hidden = true;
	            				self.imageThumbnailsContainer.style.display = "none";
            				}
						}
       				}
        		}
        	);
		};
		
		/**
		 * Mostra effettivamente le immagini, se presenti, e gestisce la visibilità
		 * e la funzionalità dei bottoni "Precedenti" e "Successive".
		 */
		
		this.updateImages = function(images) { 
			
	      	let row, image, idcell, imageTitleToShow, anchor;
	      	this.imageThumbnailsBody.innerHTML = ""; // svuota il corpo della tabella
	     	
	     	if(images.length > (range+1)*5)
				document.getElementById("nextButton").style.visibility = "visible";
			else
				document.getElementById("nextButton").style.visibility = "hidden";
			
			if(range>0)
				document.getElementById("previousButton").style.visibility = "visible";
			else
				document.getElementById("previousButton").style.visibility = "hidden";
			
			if(images.length == 0){
				document.getElementById("imageThumbnailsFieldset").hidden = true;
				this.imageThumbnailsContainer.style.display = "none";
				document.getElementById("nextButton").style.visibility = "hidden"
				document.getElementById("previousButton").style.visibility = "hidden";
				document.getElementById("commentsContainer").style.display = "none";
				document.getElementById("noImages").style.display = "inline";
				document.getElementById("noImages").textContent = "Quest'album non contiene immagini.";
			}     	
	     	else{
				
				row = document.createElement("tr");
				var dim_min = (range*5);
				var dim_max = ((range+1)*5);
				var i;
				
				for(i=dim_min; i<dim_max && i<images.length; i++){
		        
			        idcell = document.createElement("td");
					
					anchor = document.createElement("a");
		        	idcell.appendChild(anchor);
		        	
		        	image = document.createElement("img");
			        image.src = "data:image/png;base64,"+images[i].image; 
			        image.style.width = "150px";
		        	anchor.appendChild(image);
		        	
		        	anchor.setAttribute("imageID", images[i].id);
		        	anchor.setAttribute("imageTitle", images[i].imageTitle);
		        	anchor.setAttribute("date", images[i].date);
		        	anchor.setAttribute("description", images[i].description);
		        	anchor.setAttribute("albumTitle", images[i].albumTitle);
		        	anchor.setAttribute("albumOwner", images[i].owner);
		        	anchor.setAttribute("imagePhoto", images[i].image); 
					anchor.setAttribute("path", images[i].path);
						
					anchor.addEventListener("mouseover", (e) => {
						
							sessionStorage.setItem('imageID', e.target.closest("a").getAttribute("imageID"));
							
							document.getElementById("imageThumbnailsContainer").style.visibility = "visible";
							document.getElementById("imageThumbnailsBody").style.visibility = "visible";
							document.getElementById("modal").style.visibility = "visible";
							
							document.getElementById("imageDetails").textContent = "Dettagli immagine";
							document.getElementById("closeDetails").textContent = "Chiudi";
							document.getElementById("commentSectionHeader").textContent = "Commenti";
							document.getElementById("commentSectionUser").textContent = "Utente";
							document.getElementById("commentSectionText").textContent = "Testo commento";
							document.getElementById("addCommentFormHeader").textContent = "Aggiungi commento";
							
		          			var modal = document.getElementById('modal');
		          			var imageContainer = document.getElementById("imageContainer");
		          			var modalContent = document.getElementById("modalContent");
		          			var body = document.getElementById("modalBody");
		          			body.innerHTML = "";
		          			imageContainer.innerHTML = "";
		          			let imageTitleDetail, dateDetail, descriptionDetail, pathDetail, albumTitleDetail, albumOwnerDetail, row, fullScaleImage;
		          			row = document.createElement("tr");
		          			
							imageTitleDetail = document.createElement("td");
							imageTitleDetail.textContent = "Titolo immagine: "+e.target.closest("a").getAttribute("imageTitle");
							modalContent.style.visibility = "hidden";
		          			row.appendChild(imageTitleDetail);
		          			body.appendChild(row);
		          			
		          			row = document.createElement("tr");
		          			albumOwnerDetail = document.createElement("td");
		          			albumOwnerDetail.textContent = "Proprietario dell'immagine: "+e.target.closest("a").getAttribute("albumOwner");
		          			row.appendChild(albumOwnerDetail); 
		          			body.appendChild(row);
		          			
		          			row = document.createElement("tr");
		          			albumTitleDetail= document.createElement("td");
		          			albumTitleDetail.textContent = "Album a cui appartiene: "+e.target.closest("a").getAttribute("albumTitle");
		          			row.appendChild(albumTitleDetail); 
		          			body.appendChild(row);
		          			
		          			row = document.createElement("tr");
		          			dateDetail = document.createElement("td");
		          			dateDetail.textContent = "Data creazione: "+e.target.closest("a").getAttribute("date");
		          			row.appendChild(dateDetail);
		          			body.appendChild(row);
		          			
		          			row = document.createElement("tr");
		          			descriptionDetail= document.createElement("td");
		          			descriptionDetail.textContent = "Descrizione: "+e.target.closest("a").getAttribute("description");
		          			row.appendChild(descriptionDetail); 
		          			body.appendChild(row);
		          			
		          			row = document.createElement("tr");
		          			pathDetail = document.createElement("td");
		          			pathDetail.textContent = "Percorso: "+e.target.closest("a").getAttribute("path");
		          			row.appendChild(pathDetail); 
		          			body.appendChild(row);
		          			
		          			fullScaleImage = document.createElement("img");
		          			fullScaleImage.id = "fullScaleImage";
		          			fullScaleImage.src = "data:image/png;base64,"+e.target.closest("a").getAttribute("imagePhoto");
		          			imageContainer.appendChild(fullScaleImage);
		          			
		          			comments.reset();
		          			comments.show();
		          			
		          			modal.style.display = "inline";
		          					
		        		}, false);
		        		
		        	var modalClose = document.getElementById("closeDetails");
		        	modalClose.onclick = function() { 
						modal.style.display = "none";
					    document.getElementById("createCommentSuccess").textContent = "";
					    document.getElementById("createCommentError").textContent = ""; 
				    }
		        	anchor.href = "#";
					
		        	imageTitleToShow = document.createElement("p");
		        	imageTitleToShow.textContent = images[i].imageTitle;
		        	idcell.appendChild(imageTitleToShow);
	
			        row.appendChild(idcell);
		        
		        } 
		
		      this.imageThumbnailsBody.appendChild(row);
	      }
	      
	    };
		
	}
	
	function Comments(_alert, _commentsContainer, _commentsBody){ 
		
		this.alert = _alert;
		this.commentsContainer=_commentsContainer;
		this.commentsBody=_commentsBody;
		
		/**
		 * Svuota il riquadro dei commenti.
		 */
		
		this.reset = function() { 
	    	this.commentsContainer.style.display = "none";
	    	document.getElementById("noCommentsYet").textContent = "Non ci sono ancora commenti."
	      	document.getElementById("commentSectionUser").style.visibility = "hidden";
	      	document.getElementById("commentSectionText").style.visibility = "hidden";
	      	document.getElementById("createCommentError").style.display = "none";
	      	document.getElementById("createCommentSuccess").style.display = "none";
	    };
	    
	    /**
	     * Ottiene i commenti da mostrare relativi alla foto selezionata.
	     */
	    
	    this.show = function() {
		
			var self = this;
			self.alert.style.display = "none";
			this.commentsContainer.style.display = "inline";
	      	
	      	makeCall("GET", "GetComments?imageID="+ window.sessionStorage.getItem("imageID"), null,
	        	function(req) {
	          		if (req.readyState == 4) {
	            		var message = req.responseText;
	            		if (req.status == 200) {
	              			var commentsToShow = JSON.parse(message);  
	              			if (commentsToShow.length == 0){
								self.reset();
	                			return;
                			}
	    					document.getElementById("noCommentsYet").textContent = "";
	              			self.update(commentsToShow); // self visible by closure
          				} else if (req.status == 403) {
		                	window.location.href = req.getResponseHeader("Location");
		                	window.sessionStorage.removeItem('username');
                  		} else {
							if(message != null){
								self.alert.style.display = "inline";
	            				self.alert.textContent = message;
	            				self.commentsContainer.style.display = "none";
            				}
	          			}
          			}
	        	}
	    	);
    	};
    	
    	/**
    	 * Mostra effettivamente i commenti relativi alla foto selezionata.
    	 */
    	
	    this.update = function(comments) {	
		
			document.getElementById("commentSectionUser").style.visibility = "visible";
		    document.getElementById("commentSectionText").style.visibility = "visible";	
			
		    this.commentsBody.innerHTML = ""; // svuota il corpo della tabella
		    var self = this;
		    
		    comments.forEach(function(comment) { // self visible here, not this
		      	
		      	let row, user, commentContent;
		      	
		    	row = document.createElement("tr"); 
	
		        user = document.createElement("td");
		        user.textContent = comment.author;
		        row.appendChild(user);
		        commentContent = document.createElement("td");
		        commentContent.textContent = comment.text;
		        row.appendChild(commentContent);
		        self.commentsBody.appendChild(row);
		        
		    });
		      
			this.commentsContainer.style.visibility = "visible";
		}
	}
	
	/**
	 * Gestisce la form che permette la creazione di un commento.
	 */

	function CreateComment(){
		
		var self = this;
		
		/**
		 * Svuota l'area di testo relativa all'aggiunta di commenti e resetta i messaggi di
		 * successo/errore.
		 */
		
		this.reset = function(){
			document.getElementById("newCommentText").textContent = "";
			document.getElementById("createCommentError").style.display = "none";
			document.getElementById("createCommentError").style.visibility = "hidden";
			document.getElementById("createCommentSuccess").style.display = "none";
			document.getElementById("createCommentSuccess").style.visibility = "hidden";
		}
		
		/**
		 * Permette l'aggiunta di un commento mediante un'apposita form.
		 */
	    
		this.registerAddCommentEvent = function(){
			
      		document.getElementById("createCommentButton").addEventListener('click', (e) => {
	
				this.reset();
	
				/* allego alla form degli input fittizi a cui assegnare le informazioni da
					inserire nel body della richiesta, altrimenti inaccessibili */
				
      			var form = e.target.closest("form");
      			
      			if(document.getElementById("newCommentText").value.length == 0 || document.getElementById("newCommentText").value == null){
      				document.getElementById("createCommentError").style.display = "inline";
					document.getElementById("createCommentError").style.visibility = "visible";
					document.getElementById("createCommentError").textContent = "Non si possono inviare commenti vuoti.";
				}
				else{
      			
	      			var imageID = document.createElement("input");
					imageID.style.display = "none";
					imageID.setAttribute("type", "text");
					imageID.setAttribute("name", "imageID");
					imageID.setAttribute("value", window.sessionStorage.getItem("imageID"));
					form.appendChild(imageID);
					
	      			if (form.checkValidity()) {
	    				makeCall("POST", "AddComment", form,    
	        				function(x) {
	            				if (x.readyState == XMLHttpRequest.DONE) {
	              					var message = x.responseText;         
	              					switch (x.status) {
	      								case 200:
	      									document.getElementById("createCommentSuccess").style.display = "inline";
											document.getElementById("createCommentSuccess").style.visibility = "visible";
						                    document.getElementById("createCommentSuccess").textContent = message;
						                    comments.show();
	                    					break;
                    					case 403:
	                      					window.location.href = req.getResponseHeader("Location");
	                      					window.sessionStorage.removeItem('username');
	                      					break;
	                    				case 400:
	                    				case 500:
	                    					document.getElementById("createCommentError").style.display = "inline";
											document.getElementById("createCommentError").style.visibility = "visible";
	                     					document.getElementById("createCommentError").textContent = message;
	                     					break;
	          						}
								}
	            			}
	           			 );
	          		} else
	    				form.reportValidity();
				}
				
				form.removeChild(imageID);
				self.reset();
				
      		}); 
  		};				
	}
}

// ----------------------------------------------------------------------------------------

/**
 * Implementazione della funzione di drag and drop.
 */

let startElement; // mantiene il riferimento all'elemento spostato

/**
 * Funzione di utilità per il CSS: rende tutte le righe di classe "notselected".
 */

function unselectRows(rowsArray) {
    for (var i = 0; i < rowsArray.length; i++) {
        rowsArray[i].className = "notselected";
    }
}

/**
 * L'evento "dragstart" si attiva quando l'utente comincia a spostare un elemento, se il
 * suo attributo "draggable" è uguale a "true".
 */
 
function dragStart(event) {
    startElement = event.target.closest("tr"); // salva la riga da spostare
}

/**
 * L'evento "dragover" si attiva quando l'utente sposta l'oggetto selezionato sopra un
 * elemento dove è lecito che l'oggetto trattenuto venga rilasciato.
 */
 
function dragOver(event) {
	// necessario affinché l'evento "drop" venga chiamato
    event.preventDefault(); 
    // salva il riferimento alla riga su cui si trova il mouse
    var dest = event.target.closest("tr"); 
    // marca tale riga con "selected" così che, grazie al CSS, all'utente sia chiaro su cosa
    // potrebbe rilasciare l'elemento selezionato
    dest.className = "selected";
}

/**
 * L'evento "dragleave" si attiva quando l'utente sposta l'oggetto selezionato sopra un
 * elemento dove NON è lecito che l'oggetto trattenuto venga rilasciato.
 */
 
function dragLeave(event) {
    // salva il riferimento alla riga su cui si trova il mouse 
    var dest = event.target.closest("tr");
    // marca tale riga con "notselected" così che, grazie al CSS, all'utente sia chiaro che
    // non dovrebbe rilasciare l'elemento selezionato dove si trova ora il mouse
    dest.className = "notselected";
}

/**
 * L'evento "drop" si attiva quando l'elemento selezionato viene rilasciato su un bersaglio
 * lecito.
 */
 
function drop(event) {
	
    // salva il riferimento alla riga su cui si desidera rilasciare l'elemento
    var dest = event.target.closest("tr");
    
    // ottiene l'indice della riga della tabella, così da avere un riferimento per cambiare
    // la posizione degli elementi della tabella
    var table = dest.closest('table'); 
    var rowsArray = Array.from(table.querySelectorAll('tbody > tr'));
    var indexDest = rowsArray.indexOf(dest);
    
	// sposta l'elemento selezionato verso la nuova posizione
    if (rowsArray.indexOf(startElement) < indexDest)
        // se l'elemento è stato spostato in basso, lo inseriamo dopo indexDest
        startElement.parentElement.insertBefore(startElement, rowsArray[indexDest + 1]);
    else
    	// se l'elemento è stato spostato in alto, lo inseriamo prima di indexDest
        startElement.parentElement.insertBefore(startElement, rowsArray[indexDest]);
        
    // resetta lo stato di tutte le righe a "notselected"
    unselectRows(rowsArray);
}	