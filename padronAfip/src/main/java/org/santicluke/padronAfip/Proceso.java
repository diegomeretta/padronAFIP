package org.santicluke.padronAfip;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.santicluke.padronAfip.model.ArchivoAfipDAO;
import org.santicluke.padronAfip.model.ArchivoExtraido;
import org.santicluke.padronAfip.model.Contribuyente;
import org.santicluke.padronAfip.model.ContribuyenteDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class Proceso {
	
	@Autowired
	ArchivoAfipDAO archivoAfipDAO;
	
	@Autowired
	ContribuyenteDAO contribuyenteDAO;
	
	public void correr() {
		log.info("Comienzo ejecución del proceso");

		ZipFileManager zipFileManager = new ZipFileManager();
	
		try {
			Date fileLastModified = archivoAfipDAO.getFileLastModified();
			if (!archivoAfipDAO.existeArchivo(fileLastModified)) {
				log.info("Obteniendo archivo");
				ArchivoExtraido archivo = zipFileManager.obtenerArchivoSituacionFiscalDesdeAFIPWeb(ArchivoAfipDAO.ARCHIVO_AFIP_URL, fileLastModified);
				
				LocalDate fecha = obtenerFechaDeArchivo(archivo.getNombre());
				log.info("LocalDate:" + fecha);
				
				// contribuyenteDAO.guardarMasivoEnBaseDeDatos(archivo.getContenido(), fecha);
		        Date fechaDate = Date.from(fecha.atStartOfDay(ZoneId.of( "America/Buenos_Aires")).toInstant());
		        contribuyenteDAO.guardarMasivoEnBaseDeDatos2(archivo.getContenido(), fechaDate);
		        archivoAfipDAO.guardarFecha(fileLastModified, fechaDate);
			} else {
				log.info("El archivo ya existe");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		log.info("Finalizo ejecución del proceso");
	}
	
	public LocalDate obtenerFechaDeArchivo(String nombreArchivo) {
	    // Buscar patrón: .YYYYMMDD.tmp al final del nombre
	    Pattern pattern = Pattern.compile("\\.(\\d{8})\\.tmp$");
	    Matcher matcher = pattern.matcher(nombreArchivo);
	    
	    if (matcher.find()) {
	        String fechaStr = matcher.group(1);
	        try {
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	            return LocalDate.parse(fechaStr, formatter);
	        } catch (Exception e) {
	        	log.error("Error al parsear fecha: " + fechaStr);
	            return null;
	        }
	    }
	    return null;
	}
	
	public List<Contribuyente> obtenerContribuyentes(String denominacion) {
		return contribuyenteDAO.retornaContribuyentesPorDenominacion(denominacion);
	}
    
}
