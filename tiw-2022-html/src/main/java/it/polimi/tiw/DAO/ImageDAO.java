package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Image;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "immagine"
 * del database.
 */

public class ImageDAO {
	
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Interroga il database per scoprire quante immagini sono già presenti, dunque fornisce
	 * il più piccolo numero progessivo disponibile da utilizzare come identificativo per
	 * un'immagine.
	 * 
	 * @return l'ID dell'immagine.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public int getImageID() throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM immagine";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // non ci sono risultati
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
	 * Date le chiavi di un album, ottiene tutte le immagini in esso contenute.
	 * 
	 * @param albumOwner proprietario dell'album.
	 * @param albumTitle titolo dell'album.
	 * @return la lista di immagini.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public List<Image> getAlbumImages(String albumOwner, String albumTitle) throws SQLException {
		String query = "SELECT * FROM immagine WHERE proprietario_album = ? AND titolo_album = ? ORDER BY data DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, albumOwner);
			pstatement.setString(2, albumTitle);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return Collections.emptyList();
				else {
					List<Image> images = new ArrayList<>();
					while(result.next()) {
						Image image = new Image();
						image.setID(result.getInt("id"));
						image.setOwner(result.getString("proprietario_album"));
						image.setAlbumTitle(result.getString("titolo_album"));
						image.setImageTitle(result.getString("titolo_immagine"));
						image.setDate(result.getTimestamp("data"));
						image.setDescription(result.getString("descrizione"));
						image.setPath(result.getString("path"));
						images.add(image);
					}
					return images;
				}
			}
		}
	}
	
	/**
	 * Dato l'ID di un'immagine, restituisce l'immagine stessa.
	 * 
	 * @param imageId l'ID dell'immagine desiderata.
	 * @return l'istanza di immagine desiderata.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public Image getImageFromId(int imageId) throws SQLException{
		String query = "SELECT * FROM immagine WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // non ci sono risultati
					return null;
				else {
					result.next();
					Image image = new Image();
					image.setID(result.getInt("id"));
					image.setOwner(result.getString("proprietario_album"));
					image.setAlbumTitle(result.getString("titolo_album"));
					image.setImageTitle(result.getString("titolo_immagine"));
					image.setDate(result.getTimestamp("data"));
					image.setDescription(result.getString("descrizione"));
					image.setPath(result.getString("path"));
				return image;
				}
			}
		}
	}
	
	/**
	 * Date le chiavi di un album e il titolo di un'immagine, restituisce tutti i possibili ID
	 * ad essi associati. In altre parole, ritorna una lista con tutti gli ID delle immagini
	 * che condividono il medesimo album e il medesimo titolo. Questo metodo viene utilizzato
	 * per aumentare la sicurezza dell'applicazione in caso di chiamate a servlet irregolari
	 * mediante le query string.
	 * 
	 * @param albumOwner proprietario dell'album.
	 * @param albumTitle titolo dell'album.
	 * @param imageTitle titolo dell'immagine.
	 * @return la lista degli ID eventualmente presenti nel database.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public List<Integer> getImageIDsFromTitle(String albumOwner, String albumTitle, String imageTitle) throws SQLException {
		String query = "SELECT * FROM immagine WHERE proprietario_album = ? AND titolo_album = ? AND titolo_immagine = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, albumOwner);
			pstatement.setString(2, albumTitle);
			pstatement.setString(3, imageTitle);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // non ci sono risultati
					return Collections.emptyList();
				else {
					List<Integer> IDs = new ArrayList<>();
					while(result.next()) {
						IDs.add(result.getInt("id"));
					}
					return IDs;
				}
			}
		}
	}
	
	/**
	 * Aggiunge una nuova riga nella tabella "immagine" del database, grazie alle informazioni
	 * contenute nel parametro.
	 * 
	 * @param image variabile contenente le informazioni da salvare nel database.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public void createImage(Image image) throws SQLException {
		String query = "INSERT INTO immagine (proprietario_album, titolo_album, titolo_immagine, data, descrizione, path) VALUES(?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, image.getOwner());
			pstatement.setString(2, image.getAlbumTitle());
			pstatement.setString(3, image.getImageTitle());
			pstatement.setTimestamp(4, image.getDate());
			pstatement.setString(5, image.getDescription());
			pstatement.setString(6, image.getPath());
			pstatement.executeUpdate();
		}
	}

}
