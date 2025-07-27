package org.santicluke.padronAfip.model;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ArchivoAfipDAO {

	private final EntityManager entityManager;
	
	public static final String ARCHIVO_AFIP_URL = "https://www.afip.gob.ar/genericos/cInscripcion/archivos/apellidoNombreDenominacion.zip";
	
	public Date getFileLastModified() throws IOException, URISyntaxException {
		URL url = new URI(ARCHIVO_AFIP_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        long lastModified = connection.getLastModified();
        connection.disconnect();
        return lastModified != 0 ? new Date(lastModified) : null;
    }
	
	public Boolean existeArchivo(Date fecha) {
        String jpql = "SELECT COUNT(a) FROM ArchivoAfip a WHERE a.fecha_archivo_zip = :fecha";
        
        try {
            Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            log.error("Error al verificar si existe el archivo para fecha {}: {}", fecha, e.getMessage(), e);
            return false;
        }
    }
	
	public void guardarFecha(Date fechaArchivoZip, Date fechaReferencia) {
		ArchivoAfip archivoAfip = new ArchivoAfip(fechaArchivoZip, fechaReferencia);
		entityManager.persist(archivoAfip);
	}
}