package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Album;
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
	
	public Image getImageFromId(int imageId) throws SQLException{
		String query = "SELECT * FROM immagine WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, imageId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
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
