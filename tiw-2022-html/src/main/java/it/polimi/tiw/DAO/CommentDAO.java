package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Comment;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "commento"
 * del database.
 */

public class CommentDAO {
	
	private Connection connection;

	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Aggiunge una nuova riga nella tabella "commento" del database, grazie alle informazioni
	 * contenute nel parametro.
	 * 
	 * @param comment variabile contenente le informazioni da salvare del commento.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public void createComment(Comment comment) throws SQLException {
		String query = "INSERT INTO commento (autore, id_immagine, numero_commento, testo) VALUES(?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, comment.getAuthor());
			pstatement.setInt(2, comment.getImageId());
			pstatement.setInt(3, getCommentNumber(comment.getImageId()));
			pstatement.setString(4, comment.getText());
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Dato l'ID di un'immagine, calcola quanti commenti sono già presenti sotto di essa,
	 * dunque fornisce il più piccolo numero progressivo disponibile.
	 * 
	 * @param imageId l'ID dell'immagine considerata.
	 * @return il nuovo "numero_commento" del commento.
	 * @throws SQLException se ci sono errori col database.
	 */
	public int getCommentNumber(int imageId) throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM commento WHERE id_immagine = ?";
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
	 * Restituisce una lista contenente tutti i commenti presenti sotto una data immagine.
	 * 
	 * @param imageId l'ID dell'immagine considerata.
	 * @return la lista di commenti.
	 * @throws SQLException se ci sono errori col database.
	 */
	public List<Comment> getImageComments(int imageId) throws SQLException {
		String query = "SELECT * FROM commento WHERE id_immagine = ? ORDER BY numero_commento ASC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return Collections.emptyList();
				else {
					List<Comment> comments = new ArrayList<>();
					while(result.next()) {
						Comment comment = new Comment();
						comment.setAuthor(result.getString("autore"));
						comment.setText(result.getString("testo"));
						comments.add(comment);
					}
					return comments;
				}
			}
		}
	}

}
