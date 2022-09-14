/**
 * Funzione di utilità che permette l'invio di una richiesta a una servlet, allegando
 * nel body una form eventualmente passata come parametro.
 */

function makeCall(method, url, formElement, cback, reset = true) {
    var req = new XMLHttpRequest(); // visibile dalla closure
    req.onreadystatechange = function() {
      cback(req)
    }; // closure, è in grado di vedere la variabile req della funzione padre makeCall
    req.open(method, url); // si impostano il metodo (GET/POST) della richiesta e l'indirizzo della servlet
    if (formElement == null) { // se nella form non ci sono dati, si invia al server la richiesta vuota
      req.send();
    } else { // altrimenti, se la form è stata compilata, si inviano i dati al server come parametri della richiesta
      req.send(new FormData(formElement));
    }
    if (formElement !== null && reset === true) { // svuota i campi della form
      formElement.reset();
    }
}