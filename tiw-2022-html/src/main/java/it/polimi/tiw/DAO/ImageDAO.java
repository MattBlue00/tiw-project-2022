package it.polimi.tiw.DAO;

import java.sql.Connection;

public class ImageDAO {
	
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}

}
