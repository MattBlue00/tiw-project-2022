package it.polimi.tiw.beans;


/**
 * Classe che contiene tutte le informazioni relative ad un immagine.
 * Rispecchia perfettamente l'analoga tabella del DB.
 *
 */
import java.sql.Timestamp;

public class Image {
	
	private int id;
	private String owner;
	private String albumTitle;
	private String imageTitle;
	private Timestamp date;
	private String description;
	private String path;
	private String image; //usato per visualizzare le immagini in javascript
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getAlbumTitle() {
		return albumTitle;
	}
	
	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}
	
	public String getImageTitle() {
		return imageTitle;
	}
	
	public void setImageTitle(String imageTitle) {
		this.imageTitle = imageTitle;
	}
	
	public Timestamp getDate() {
		return date;
	}
	
	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
}
