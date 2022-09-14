package it.polimi.tiw.beans;


/**
 * Classe che contiene tutte le informazioni relative ad un commento.
 * Rispecchia perfettamente l'analoga tabella del DB.
 *
 */
public class Comment {
	
	private String author;
	private int imageId;
	private int commentNumber;
	private String text;
	
	public String getAuthor() { return author; }
	
	public void setAuthor(String owner) { this.author = owner; }
	
	public int getImageId() { return imageId; }
	
	public void setImageId(int imageId) { this.imageId = imageId; }
	
	public int getCommentNumber() { return commentNumber; }
	
	public void setCommentNumber(int commentNumber) { this.commentNumber = commentNumber; }
	
	public String getText() { return text; }
	
	public void setText(String text) { this.text = text; }
	
}