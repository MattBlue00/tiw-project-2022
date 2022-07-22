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
