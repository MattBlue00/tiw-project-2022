package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

/**
 * Data Access Object che permette l'interrogazione e l'aggiornamento della tabella "utente" del DB
 *
 */
public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Il metodo controlla se username e password inseriti sono presenti nel DB.
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
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
	 * Il metodo controlla se esiste già un utente con lo stesso username o email dell'utente che vuole registrarsi.
	 * @param email
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public boolean checkCredentialsRegistration(String email, String username) throws SQLException {
		String query = "SELECT * FROM utente WHERE username = ? OR email =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return true; // everything is ok
				else {
					return false;
				}
			}
		}
	}
	
	/**
	 * Il metodo inserisce un nuovo utente nella tabella 'utente' del DB.
	 * @param user
	 * @throws SQLException
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
