package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

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
	 * This method checks if other users have the same username and/or the same email.
	 * If the username and/or the email are not unique checkCredentialsRegistration returns false, 
	 * otherwise returns true.
	 * 
	 * @param email - email of the user who is trying to register.
	 * @param username - username of the user who is trying to register.
	 * @return a boolean: true if the username and the email are unique, otherwise false .
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
	 * This method register a new user.
	 * 
	 * @param username - unique username that the user chose.
	 * @param email - unique email that the user chose.
	 * @param password - password that the user chose.
	 * @return
	 * @throws SQLException
	 */
	public void registerUser(String username, String email, String password) throws SQLException {
		String query = "INSERT INTO utente (username, email, password) VALUES(?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);
			pstatement.executeUpdate();
		}
	}
	
	
	
	
	
	
	
	
	
	
}
