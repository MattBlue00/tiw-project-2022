package it.polimi.tiw.DAO;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "album" del DB
 *
 */
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

	/**
	 * Il metodo ritorna tutti gli album dell'utente passato come parametro.
	 * Gli album sono ordinati per data di creazione decrescente.
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public List<Album> getUserAlbumsByDate(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario = ? ORDER BY data_creazione DESC";
		List<Album> userAlbums = buildAlbumList(query, username);
		return userAlbums;
	}
	
	/**
	 * Il metodo ritorna tutti gli album dell'utente passato come parametro.
	 * Gli album sono ordinati secondo l'ordine indicato dall'attributo 'ordine' associato ad ogni album.
	 * Nel caso in cui l'attributo 'ordine' dovesse essere null si ordina per data di creazione decrescente.
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public List<Album> getUserAlbums(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario = ? ORDER BY ordine ASC, data_creazione DESC";
		List<Album> userAlbums = buildAlbumList(query, username);
		return userAlbums;
	}
	
	/**
	 * Il metodo ritorna tutti gli album che non sono dell'utente passato come parametro.
	 * Gli album sono ordinati per data di creazione decrescente.
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public List<Album> getOtherAlbums(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario <> ? ORDER BY data_creazione DESC";
		List<Album> otherAlbums = buildAlbumList(query, username);
		return otherAlbums;
	}
	
	/**
	 * Metodo di appoggio di GetUserAlbums, GetUserAlbumsByDate e GetOtherAlbums.
	 * @param query
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	private List<Album> buildAlbumList(String query, String username) throws SQLException {
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return Collections.emptyList();
				else {
					List<Album> albums = new ArrayList<>();
					while(result.next()) {
						// vengono settati tutti i parametri dell'album da aggiungere alla lista di album
						Album album = new Album();
						album.setOwner(result.getString("proprietario"));
						album.setTitle(result.getString("titolo"));
						album.setCreationDate(result.getTimestamp("data_creazione"));
						albums.add(album);
					}
					return albums;
				}
			}
		}
	}
	
	/** 
	 * Il metodo inserisce nel DB il nuovo album appena creato.
	 * @param album
	 * @throws SQLException
	 */
	public void createAlbum(Album album) throws SQLException {
		String query = "INSERT INTO album (proprietario, titolo, data_creazione) VALUES(?, ?, CURRENT_TIMESTAMP())";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, album.getOwner());
			pstatement.setString(2, album.getTitle());
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * Il metodo controlla se esistono altri album con lo stesso titolo e dello stesso proprietario dell'album passato come parametro.
	 * Se non esistono altri album il metodo ritorna true, altrimenti ritorna false.
	 * @param album
	 * @return boolean
	 * @throws SQLException
	 */
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
	
	/**
	 * Il metodo aggiorna il DB, aggiornando l'attributo "ordine" dell'album passato come parametro.
	 * @param newIndex, nuovo indice dell'album
	 * @param albumOwner, proprietario dell'album
	 * @param albumTitle, titolo dell'album
	 * @throws SQLException
	 */
	public void modifyAlbumOrder(int newIndex, String albumOwner, String albumTitle) throws SQLException{
		String query = "UPDATE album SET ordine = ? WHERE proprietario = ? AND titolo = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, newIndex);
			pstatement.setString(2, albumOwner);
			pstatement.setString(3, albumTitle);
			pstatement.executeUpdate();
		}
	}
}
