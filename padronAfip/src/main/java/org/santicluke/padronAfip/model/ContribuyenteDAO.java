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
	
	public void guardarMasivoEnBaseDeDatos(byte[] contenidoArchivo, LocalDate fechaArchivo) {
	    
	    Map<String, Integer> referencias = new HashMap<String, Integer>();
	    referencias.put("  ", 0);
	    referencias.put("NI", 1);
	    referencias.put("AC", 2);
	    referencias.put("EX", 3);
	    referencias.put("NA", 4);
	    referencias.put("XN", 5);
	    referencias.put("AN", 6);
	    referencias.put("NC", 7);
	
	    // Convertir byte array a BufferedReader usando ByteArrayInputStream
	    try (ByteArrayInputStream bais = new ByteArrayInputStream(contenidoArchivo);
	         InputStreamReader isr = new InputStreamReader(bais, StandardCharsets.ISO_8859_1);
	         BufferedReader reader = new BufferedReader(isr)) {
	        
	        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        log.info("Guardar en bbdd -> START :" + df.format(new java.util.Date()));
	        Connection con = Mariadb.getConnection();
	        PreparedStatement preparedStmt = null;
	        int batchSize = 1000;
	        int count = 0;
	        int updateCount = 0;
	        int insertCount = 0;
	
	        try {
	            con.setAutoCommit(false);
	            
	            String sql = "INSERT INTO contribuyentes (cuit, denominacion, impuesto_ganancias_id, impuesto_iva_id, " +
	                        "monotributo, integrante_sociedades, empleador, actividad_monotributo, fecha) " +
	                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
	                        "ON DUPLICATE KEY UPDATE " +
	                        "denominacion = VALUES(denominacion), " +
	                        "impuesto_ganancias_id = VALUES(impuesto_ganancias_id), " +
	                        "impuesto_iva_id = VALUES(impuesto_iva_id), " +
	                        "monotributo = VALUES(monotributo), " +
	                        "integrante_sociedades = VALUES(integrante_sociedades), " +
	                        "empleador = VALUES(empleador), " +
	                        "actividad_monotributo = VALUES(actividad_monotributo), " +
	                        "fecha = VALUES(fecha)";
	
	            preparedStmt = con.prepareStatement(sql);
	            java.sql.Date fecha = java.sql.Date.valueOf(fechaArchivo);
	
	            String linea;
	            while ((linea = reader.readLine()) != null) {
	                preparedStmt.setString(1, linea.substring(0, 11)); // cuit
	                preparedStmt.setString(2, linea.substring(11, 41).trim()); // denominacion
	                
	                Integer impGanancias = referencias.get(linea.substring(41, 43));
	                preparedStmt.setInt(3, impGanancias); // impGanancias
	                preparedStmt.setInt(4, referencias.get(linea.substring(43, 45))); // impIva
	                preparedStmt.setString(5, linea.substring(45, 47)); // monotributo
	                preparedStmt.setString(6, linea.substring(47, 48)); // integranteSoc
	                preparedStmt.setString(7, linea.substring(48, 49)); // empleador
	                preparedStmt.setString(8, linea.substring(49, 51)); // actividadMonotributo
	                preparedStmt.setDate(9, fecha);
	
	                preparedStmt.addBatch();
	                count++;
	
	                if (count % batchSize == 0) {
	                    int[] results = preparedStmt.executeBatch();
	                    // Contar inserts y updates
	                    for (int result : results) {
	                        if (result == 1) insertCount++;
	                        else if (result == 2) updateCount++;
	                    }
	                    con.commit();
	                    preparedStmt.clearBatch();
	                    if (count % (batchSize * 10) == 0) {
	                    	System.out.println("Procesados " + count + " registros - Insertados: " + insertCount + " - Actualizados: " + updateCount);
	                    }
	                }
	            }
	
	            if (count % batchSize != 0) {
	                int[] results = preparedStmt.executeBatch();
	                for (int result : results) {
	                    if (result == 1) insertCount++;
	                    else if (result == 2) updateCount++;
	                }
	                con.commit();
	            }
	
	            System.out.println("Proceso completado - Total insertados: " + insertCount + " - Total actualizados: " + updateCount);
	            preparedStmt.close();
	            con.close();
	
	        } catch (IOException | SQLException e) {
	            e.printStackTrace();
	            try {
	                if (con != null) con.rollback();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        }
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
	}
	
	@Transactional
    public void guardarMasivoEnBaseDeDatos2(byte[] contenidoArchivo, Date fechaArchivo) {
        
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