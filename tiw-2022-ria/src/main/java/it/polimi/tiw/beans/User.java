package it.polimi.tiw.beans;


/**
 * Classe che contiene tutte le informazioni relative ad un utente.
 * Rispecchia perfettamente l'analoga tabella del DB.
 *
 */
public class User {

	private String username;
	private String password;
	private String email;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
}
