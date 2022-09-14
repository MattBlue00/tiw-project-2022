package it.polimi.tiw.DAO;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import it.polimi.tiw.beans.Image;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "immagine" del DB
 *
 */
public class ImageDAO {
	
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Il metodo ritorna il numero di immagini + 1 presenti nella tabella 'immagine' del DB.
	 * Il risultato sarà il nuovo ID dell'immagine che si vuole creare.
	 * @return
	 * @throws SQLException
	 */
	public int getImageID() throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM immagine";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
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
	 * Il metodo ritorna tutte le immagini appartenenti ad un album (dati proprietario e titolo dell'album).
	 * @param albumOwner
	 * @param albumTitle
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public List<Image> getAlbumImages(String albumOwner, String albumTitle) throws SQLException, IOException {
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
						byte[] fileContent = FileUtils.readFileToByteArray(new File(image.getPath()));
						String encodedString = Base64.getEncoder().encodeToString(fileContent);
						image.setImage(encodedString);
						images.add(image);
					}
					return images;
				}
			}
		}
	}
	
	/**
	 * Il metodo aggiorna il DB inserendo una nuova immagine nella tabella 'immagine'.
	 * @param image
	 * @throws SQLException
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
