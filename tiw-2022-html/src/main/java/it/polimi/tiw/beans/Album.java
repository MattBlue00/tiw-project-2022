package it.polimi.tiw.beans;

import java.sql.Timestamp;

/**
 * Classe che contiene tutte le informazioni d'interesse relative a un album. Rispecchia
 * perfettamente l'analoga tabella del database.
 */

public class Album {
	
	private String owner;
	private String title;
	private Timestamp creationDate;
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Timestamp getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

}
