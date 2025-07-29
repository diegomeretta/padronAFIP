package org.santicluke.padronAfip.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ContribuyenteDAO {

	private final EntityManager entityManager;
	
	@Transactional
    public void guardarMasivoEnBaseDeDatos(byte[] contenidoArchivo, Date fechaArchivo) {
        
        Map<String, Integer> referencias = new HashMap<>();
        referencias.put("  ", 0);
        referencias.put("NI", 1);
        referencias.put("AC", 2);
        referencias.put("EX", 3);
        referencias.put("NA", 4);
        referencias.put("XN", 5);
        referencias.put("AN", 6);
        referencias.put("NC", 7);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(contenidoArchivo);
             InputStreamReader isr = new InputStreamReader(bais, StandardCharsets.ISO_8859_1);
             BufferedReader reader = new BufferedReader(isr)) {
            
            log.info("Guardar en bbdd -> START: {}", java.time.LocalDateTime.now());
            
            int batchSize = 1000;
            int count = 0;

            String linea;
            while ((linea = reader.readLine()) != null) {
                Contribuyente contribuyente = parsearLinea(linea, referencias, fechaArchivo);
                
                // Merge para hacer upsert
                entityManager.merge(contribuyente);
                count++;

                if (count % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear(); // Limpiar el contexto de persistencia
                    
                    if (count % (batchSize * 10) == 0) {
                        log.info("Procesados {} registros", count);
                    }
                }
            }
            entityManager.flush();
            log.info("Proceso completado - Total procesados: {}", count);
            
        } catch (IOException e) {
            log.error("Error procesando archivo", e);
            throw new RuntimeException("Error procesando archivo masivo", e);
        }
    }
    
    private Contribuyente parsearLinea(String linea, Map<String, Integer> referencias, Date fechaArchivo) {
        String cuit = linea.substring(0, 11);
        String denominacion = linea.substring(11, 41).trim();
        Integer impGanancias = referencias.get(linea.substring(41, 43));
        Integer impIva = referencias.get(linea.substring(43, 45));
        String monotributo = linea.substring(45, 47);
        String integranteSoc = linea.substring(47, 48);
        String empleador = linea.substring(48, 49);
        String actividadMonotributo = linea.substring(49, 51);
        
        return new Contribuyente(cuit, denominacion, impGanancias, impIva, 
                               monotributo, integranteSoc, empleador, actividadMonotributo, fechaArchivo);
    }

	public List<Contribuyente> retornaContribuyentesPorDenominacion(String denominacion) {
		String jpql = "SELECT c FROM Contribuyente c WHERE c.denominacion LIKE :denominacion";
	    TypedQuery<Contribuyente> query = entityManager.createQuery(jpql, Contribuyente.class);
	    query.setParameter("denominacion", "%" + denominacion + "%");
	    return query.getResultList();
	}

}