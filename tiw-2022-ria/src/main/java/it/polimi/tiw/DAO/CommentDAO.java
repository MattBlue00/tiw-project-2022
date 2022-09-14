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
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "commento" del DB
 *
 */
public class CommentDAO {
	
	private Connection connection;

	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Il metodo aggiorna il DB inserendo un nuovo commento nella tabella "commento".
	 * 
	 * @param comment
	 * @throws SQLException
	 */
	public void createComment(Comment comment) throws SQLException {
		String query = "INSERT INTO commento (autore, id_immagine, numero_commento, testo) VALUES(?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, comment.getAuthor());
			pstatement.setInt(2, comment.getImageId());
			pstatement.setInt(3, comment.getCommentNumber());
			pstatement.setString(4, comment.getText());
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Il metodo ritorna il numero di commenti + 1 di una data immagine (dato il suo id).
	 * 
	 * @param imageId
	 * @return
	 * @throws SQLException
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
					// contiamo tutti i commenti
					int count = result.getInt("total");
					return count + 1;
				}
			}
		}
	}
	
	/**
	 * Il metodo ritorna tutti i commenti di una data immagine (dato il suo id).
	 * 
	 * @param imageId
	 * @return
	 * @throws SQLException
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
						// vengono settati tutti i parametri del commento da aggiungere alla lista di commenti
						comment.setAuthor(result.getString("autore"));
						comment.setImageId(imageId);
						comment.setCommentNumber(result.getInt("numero_commento"));
						comment.setText(result.getString("testo"));
						comments.add(comment);
					}
					return comments;
				}
			}
		}
	}

}