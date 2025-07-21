package org.santicluke.padronAfip.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.santicluke.padronAfip.ConfigManager;

public class Mariadb {

	private Mariadb() {
	}

	static public Connection getConnection() {
		String url = ConfigManager.getProperty("database.url");
		String username = ConfigManager.getProperty("database.username");
		String password = ConfigManager.getProperty("database.password");

		try {
			Class.forName("org.mariadb.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot find the driver in the classpath!", e);
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot connect the database!", e);
		}
		return connection;
	}

}