package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Comment {
	
	private String owner;
	private int imageId;
	private int commentNumber;
	private String text;
	
	public String getOwner() { return owner; }
	
	public void setOwner(String owner) { this.owner = owner; }
	
	public int getImageId() { return imageId; }
	
	public void setImageId(int imageId) { this.imageId = imageId; }
	
	public int getCommentNumber() { return commentNumber; }
	
	public void setCommentNumber(int commentNumber) { this.commentNumber = commentNumber; }
	
	public String getText() { return text; }
	
	public void setText(String text) { this.text = text; }
	
	
}
