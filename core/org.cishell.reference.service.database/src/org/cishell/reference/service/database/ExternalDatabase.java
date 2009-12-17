package org.cishell.reference.service.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.cishell.service.database.Database;

public class ExternalDatabase implements Database {

	private DataSource dataSource;
	
	public ExternalDatabase(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	/*
	 * TODO: We could store the password from the beginning. 
	 * Will want to discuss security implications here.
	 */
	public Connection getConnection(String username, String password) 
		throws SQLException {
		return dataSource.getConnection(username, password);
	}
}
