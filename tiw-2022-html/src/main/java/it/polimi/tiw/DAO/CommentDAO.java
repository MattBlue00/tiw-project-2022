package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Comment;

public class CommentDAO {
	
	private Connection connection;

	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Questo metodo aggiunge un commento alla tabella "commento" nel database
	 * 
	 * @param comment
	 * @throws SQLException
	 */
	public void createComment(Comment comment) throws SQLException {
		String query = "INSERT INTO commento (autore, id_immagine, numero_commento, testo) VALUES(?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, comment.getOwner());
			pstatement.setInt(2, comment.getImageId());
			pstatement.setInt(3, getCommentNumber(comment.getImageId()));
			pstatement.setString(4, comment.getText());
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Questo metodo ritorna il numero di commenti di una data immagine (dato il suo id)
	 * 
	 * @param imageId
	 * @return
	 * @throws SQLException
	 */
	public int getCommentNumber(int imageId) throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM immagine WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return 1;
				else {
					result.next();
					int count = result.getInt("total");
					return count + 1;
				}
			}
		}
	}
	
	/**
	 * Questo metodo ritorna tutti i commenti di una data immagine (dato il suo id)
	 * 
	 * @param imageId
	 * @return
	 * @throws SQLException
	 */
	public List<Comment> getImageComments(int imageId) throws SQLException {
		String query = "SELECT * FROM commento WHERE id_immagine = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return Collections.emptyList();
				else {
					List<Comment> comments = new ArrayList<>();
					while(result.next()) {
						Comment comment = new Comment();
						comment.setOwner(result.getString("autore"));
						comment.setText(result.getString("testo"));
						comments.add(comment);
					}
					return comments;
				}
			}
		}
	}

}
