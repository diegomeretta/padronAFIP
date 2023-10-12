package org.santicluke.padronAfip;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ContribuyenteDAO {

	public static void registrarCredencial(Integer numeroMatricula, Integer cantidad) {
		String sql = "update tmtr028_kinesiologo set MTR028_CARNET = " + cantidad + " where MTR028_N_MATRICULA = "
				+ numeroMatricula;
		try {
			Connection con = Mariadb.getConnection();
			Statement stmt = con.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}