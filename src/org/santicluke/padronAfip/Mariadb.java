package org.santicluke.padronAfip;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mariadb {

	private Mariadb() {
	}

	static public Connection getConnection() {
		String url = "jdbc:mariadb://192.168.16.11:3306/padron";
		String username = "usuario";
		String password = "contrasena";

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