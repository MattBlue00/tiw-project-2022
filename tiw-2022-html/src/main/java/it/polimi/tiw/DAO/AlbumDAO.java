package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Album;

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
						album.setCreationDate(result.getDate("data_creazione"));
						albums.add(album);
					}
					return albums;
				}
			}
		}
	}
}
