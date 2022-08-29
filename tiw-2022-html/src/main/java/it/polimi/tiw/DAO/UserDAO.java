package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "utente"
 * del database.
 */

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Controlla che nel database esista un utente con l'username e la password forniti
	 * in fase di login. Nel caso fosse così, restituisce un'istanza di {@link User} contenente
	 * l'username.
	 * 
	 * @param username l'username fornito.
	 * @param password la password fornita.
	 * @return un'istanza di {@link User}.
	 * @throws SQLException se ci sono errori col database.
	 */

	public User checkCredentials(String username, String password) throws SQLException {
		String query = "SELECT * FROM utente WHERE username = ? AND password =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setUsername(result.getString("username"));
					return user;
				}
			}
		}
	}
	
	/**
	 * Controlla se nel database non esista un utente con l'username o l'email passati come
	 * parametri.
	 * 
	 * @param email l'email fornita.
	 * @param username l'username fornito.
	 * @return {@code true} se tale utente non esiste, {@code false} altrimenti.
	 * @throws SQLException se ci sono errori col database.
	 */
	
	public boolean checkCredentialsRegistration(String email, String username) throws SQLException {
		String query = "SELECT * FROM utente WHERE username = ? OR email =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // non ci sono risultati
					return true; // l'utente si può registrare
				else {
					return false;
				}
			}
		}
	}
	
	/**
	 * Aggiunge una nuova riga nella tabella "utente" del database, grazie alle informazioni
	 * contenute nel parametro.
	 * 
	 * @param user l'utente da registrare.
	 * @throws SQLException se ci sono errori col database.
	 */

	public void registerUser(User user) throws SQLException {
		String query = "INSERT INTO utente (username, email, password) VALUES(?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, user.getUsername());
			pstatement.setString(2, user.getEmail());
			pstatement.setString(3, user.getPassword());
			pstatement.executeUpdate();
		}
	}
	
}
