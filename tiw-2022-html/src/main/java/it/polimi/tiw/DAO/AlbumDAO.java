package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;

public class AlbumDAO {
	
	private Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Album> getUserAlbums(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario = ? ORDER BY data_creazione DESC";
		List<Album> userAlbums = buildAlbumList(query, username);
		return userAlbums;
	}
	
	public List<Album> getOtherAlbums(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario <> ? ORDER BY data_creazione DESC";
		List<Album> otherAlbums = buildAlbumList(query, username);
		return otherAlbums;
	}
	
	private List<Album> buildAlbumList(String query, String username) throws SQLException {
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return Collections.emptyList();
				else {
					List<Album> albums = new ArrayList<>();
					while(result.next()) {
						Album album = new Album();
						album.setOwner(result.getString("proprietario"));
						album.setTitle(result.getString("titolo"));
						albums.add(album);
					}
					return albums;
				}
			}
		}
	}
	
	public void createAlbum(Album album) throws SQLException {
		String query = "INSERT INTO album (proprietario, titolo, data_creazione) VALUES(?, ?, CURRENT_TIMESTAMP())";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, album.getOwner());
			pstatement.setString(2, album.getTitle());
			pstatement.executeUpdate();
		}
	}
	
	public Album searchAlbumFromTitle(String albumTitle, String owner) throws SQLException {
		String query = "SELECT * FROM album WHERE titolo = ? AND proprietario = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, albumTitle);
			pstatement.setString(2, owner);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return null;
				else {
					result.next();
					Album album = new Album();
					album.setOwner(result.getString("proprietario"));
					album.setTitle(result.getString("titolo"));
					album.setCreationDate(result.getTimestamp("data_creazione"));
					return album;
				}
			}
		}
	}
	
	public boolean checkAlbumTitle(Album album) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario = ? AND titolo = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, album.getOwner());
			pstatement.setString(2, album.getTitle());
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return true; // the album can be created
				else {
					return false;
				}
			}
		}
	}
}
