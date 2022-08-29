package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polimi.tiw.beans.Album;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "album"
 * del database.
 */

public class AlbumDAO {
	
	private Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Ottiene la lista di album di proprietà dell'utente passato come parametro.
	 * 
	 * @param username l'username dell'utente correntemente loggato.
	 * @return la lista di album (eventualmente vuota).
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public List<Album> getUserAlbums(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario = ? ORDER BY data_creazione DESC";
		List<Album> userAlbums = buildAlbumList(query, username);
		return userAlbums;
	}
	
	/**
	 * Ottiene la lista di album NON di proprietà dell'utente passato come parametro.
	 * 
	 * @param username l'username dell'utente correntemente loggato.
	 * @return la lista di album (eventualmente vuota).
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public List<Album> getOtherAlbums(String username) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario <> ? ORDER BY data_creazione DESC";
		List<Album> otherAlbums = buildAlbumList(query, username);
		return otherAlbums;
	}
	
	/**
	 * Metodo ausiliario che, data una query, interroga il database e costruisce una lista
	 * di album.
	 * 
	 * @param query la query da eseguire nel database.
	 * @param username l'username dell'utente correntemente loggato.
	 * @return la lista di album (eventualmente vuota).
	 * @throws SQLException se ci sono errori col database.
	 */
	
	private List<Album> buildAlbumList(String query, String username) throws SQLException{
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // non ci sono risultati
					return Collections.emptyList();
				else {
					List<Album> albums = new ArrayList<>();
					while(result.next()) {
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
	 * Aggiunge una nuova riga nella tabella "album" del database, grazie alle informazioni
	 * contenute nel parametro e alla funzione CURRENT_TIMESTAMP() - che fornisce la data e
	 * l'ora di creazione dell'album.
	 * 
	 * @param album variabile contenente il titolo e il proprietario dell'album.
	 * @throws SQLException se ci sono errori col database.
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
	 * Controlla che un album con le stesse chiavi di quello passato come parametro
	 * non esista già nel database.
	 * 
	 * @param album album da contollare.
	 * @return {@code true} se non esiste un album con quelle chiavi, {@code false} altrimenti.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public boolean checkAlbumTitle(Album album) throws SQLException {
		String query = "SELECT * FROM album WHERE proprietario = ? AND titolo = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, album.getOwner());
			pstatement.setString(2, album.getTitle());
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // non ci sono risultati
					return true; // l'album può essere creato
				else {
					return false;
				}
			}
		}
	}
}
