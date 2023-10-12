package org.santicluke.padronAfip;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

	public static void main(String[] args) throws IOException {
		System.out.println("Comienzo ejecución del sistema");

		// byte[] archivo = obtenerArchivoSituacionFiscalDesdeAFIPWeb();

		byte[] buffer = new byte[4096];
		String filePath = "src/org/santicluke/padronAfip/SELE-SAL-CONSTA.p20out1.20231007.tmp";
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int bytes = 0;
		while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
			baos.write(buffer, 0, bytes);
		}
		baos.close();
		bis.close();
		byte[] byteArray = baos.toByteArray();

		guardarDatosEnBaseDeDatos(byteArray);

		System.out.println("Finalizo ejecución del sistema");
	}

	public static void guardarDatosEnBaseDeDatos(byte[] file) {
		InputStream is = null;
		BufferedReader bfReader = null;
		Map<String, Integer> referencias = new HashMap<String, Integer>();
		referencias.put("  ", 0);
		referencias.put("NI", 1);
		referencias.put("AC", 2);
		referencias.put("EX", 3);
		referencias.put("NA", 4);
		referencias.put("XN", 5);
		referencias.put("AN", 6);
		referencias.put("NC", 7);
		try {
			is = new ByteArrayInputStream(file);
			bfReader = new BufferedReader(new InputStreamReader(is));
			String bufferReadLine = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println("Guardar en bbdd -> START :" + df.format(new Date()));
			try {
				Connection con = Mariadb.getConnection();
				PreparedStatement preparedStmt = null;
//				int i = 0;
				while ((bufferReadLine = bfReader.readLine()) != null) {
					String sql = " insert into contribuyentes (cuit, denominacion, impuesto_ganancias_id, impuesto_iva_id, monotributo,"
							+ "	integrante_sociedades, empleador, actividad_monotributo)"
							+ " values (?, ?, ?, ?, ?, ?, ?, ?)";
					preparedStmt = con.prepareStatement(sql);
					preparedStmt.setString(1, bufferReadLine.substring(0, 11)); // cuit
					preparedStmt.setString(2, bufferReadLine.substring(11, 41).trim()); // denominacion
					Integer impGanancias = referencias.get(bufferReadLine.substring(41, 43));
					preparedStmt.setInt(3, impGanancias); // impGanancias
					preparedStmt.setInt(4, referencias.get(bufferReadLine.substring(43, 45))); // impIva
					preparedStmt.setString(5, bufferReadLine.substring(45, 47)); // monotributo
					preparedStmt.setString(6, bufferReadLine.substring(47, 48)); // integranteSoc
					preparedStmt.setString(7, bufferReadLine.substring(48, 49)); // empleador
					preparedStmt.setString(8, bufferReadLine.substring(49, 51)); // actividadMonotributo

					preparedStmt.execute();
//					if (i % 50 == 0) {
//						preparedStmt.executeBatch();
//					}
//					i++;
				}
//				preparedStmt.executeBatch();
				System.out.println("Guardar en bbdd -> FINISH :" + df.format(new Date()));
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			bfReader.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static byte[] obtenerArchivoSituacionFiscalDesdeAFIPWeb() {

		try {
			// ByteArrayOutputStream writer = downloadAfipZipFile();

			byte[] buffer = new byte[4096];
			String filePath = "src/org/santicluke/padronAfip/apellidoNombreDenominacion.zip";
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int bytes = 0;
			while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
				baos.write(buffer, 0, bytes);
			}
			baos.close();
			bis.close();
			byte[] byteArray = baos.toByteArray();
			System.out.println("byteArray:" + byteArray.length);
			return extraerArchivoDeZip(byteArray);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ByteArrayOutputStream downloadAfipZipFile() throws MalformedURLException, IOException {
		System.out.println("Downloading ZIP.");
		byte[] buffer = new byte[102400];
		int totalBytesRead = 0;
		int bytesLeidos = 0;
		URL url = new URL("https://www.afip.gob.ar/genericos/cInscripcion/archivos/apellidoNombreDenominacion.zip");

		url.openConnection();
		InputStream reader = url.openStream();
		ByteArrayOutputStream writer = new ByteArrayOutputStream();

		while ((bytesLeidos = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesLeidos);
			buffer = new byte[1024];
			totalBytesRead += bytesLeidos;
		}

		reader.close();
		writer.close();
		return writer;
	}

	private static byte[] extraerArchivoDeZip(byte[] zipByteArray) throws IOException {
		System.out.println("Extracting ZIP.");
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipByteArray));
		ZipEntry entrada;
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		// Revisar
		while (null != (entrada = zip.getNextEntry())) {
			System.out.println("entrada:" + entrada);

			if (entrada.getName().contains("SELE-SAL-CONSTA")) {
				fos = new ByteArrayOutputStream();
				int leido;
				byte[] buff = new byte[1024];
				while (0 < (leido = zip.read(buff))) {
					fos.write(buff, 0, leido);
				}
				fos.close();
				zip.close();
				return fos.toByteArray();
			}
		}
		zip.close();
		return null;

	}

	private static String obtenerTextoValorRegistroAFIP(String valor) {

		switch (valor) {
		case "NI":
			return "No Inscripto";
		case "AC":
			return "Activo";
		case "EX":
			return "Exento";
		case "NA":
			return "No alcanzado";
		case "XN":
			return "Exento no alcanzado";
		case "AN":
			return "Activo no alcanzado";
		case "NC":
			return "No corresponde";
		default:
			return valor;
		}
	}
}
