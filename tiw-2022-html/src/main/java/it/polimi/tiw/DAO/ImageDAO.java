package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.Image;

public class ImageDAO {
	
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int getImageID() throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM immagine";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					// TODO is this the right thing to do?
					return 1;
				else {
					result.next();
					int count = result.getInt("total");
					return count + 1;
				}
			}
		}
	}
	
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
