package org.santicluke.padronAfip;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.santicluke.padronAfip.model.ArchivoExtraido;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ZipFileManager {
	
	public ArchivoExtraido obtenerArchivoSituacionFiscalDesdeAFIPWeb(String afipFileUrl, Date fileLastModified) {

		try {
            URI downloadedFileUri = downloadAfipZipFile(afipFileUrl, fileLastModified);
            Path filePath = Paths.get(downloadedFileUri);
            log.debug("filePath:" + filePath.toString());
            ArchivoExtraido extractedContent = extraerArchivoDeZipDesdeArchivo(filePath);
            return extractedContent;
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
        	e.printStackTrace();
        }
        return null;
	}

    public URI downloadAfipZipFile(String afipFileUrl, Date fileLastModified) throws MalformedURLException, IOException, URISyntaxException {
        log.info("Downloading ZIP from: " + afipFileUrl);
        
        Path downloadDir = Paths.get("downloads");
        Files.createDirectories(downloadDir);
        
        String fileName = generateFileName(afipFileUrl, fileLastModified);
        Path filePath = downloadDir.resolve(fileName);
        
        if (Files.exists(filePath)) {
            log.info("File already exists, skipping download: " + fileName);
            return filePath.toUri();
        }
        
        URL url = new URI(afipFileUrl).toURL();
        try (InputStream inputStream = url.openStream()) {
            long bytesDownloaded = Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Total bytes downloaded: " + bytesDownloaded);
        }
        
        return filePath.toUri();
    }
    
    private String generateFileName(String url, Date fileLastModified) {
        String baseName = "afip_file";
        if (url.contains("/")) {
            String urlPart = url.substring(url.lastIndexOf("/") + 1);
            if (urlPart.contains(".")) {
                baseName = urlPart.substring(0, urlPart.lastIndexOf("."));
            }
        }
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = df.format(fileLastModified);
        return baseName + "_" + timestamp + ".zip";
    }

    private ArchivoExtraido extraerArchivoDeZipDesdeArchivo(Path zipFilePath) throws IOException {
    	log.debug("Extracting ZIP from file: " + zipFilePath);
        long fileSize = Files.size(zipFilePath);
        log.debug("Tamaño del archivo: " + fileSize + " bytes");
        if (fileSize == 0) {
            log.error("ERROR: El archivo está vacío");
        }
        log.debug("Permisos de lectura: " + Files.isReadable(zipFilePath));
        
        FileInputStream fis = new FileInputStream(zipFilePath.toFile());
        byte[] header = new byte[4];
        int bytesRead = fis.read(header);
        if (bytesRead >= 2) {
            if (header[0] != 0x50 || header[1] != 0x4B) {
                log.error("ERROR: No es un archivo ZIP válido (esperado: 0x504B)");
            }
        }
        
        ZipInputStream zip = new ZipInputStream(fis);
    	log.debug("Procesando archivo zip");
    	ZipEntry entrada = zip.getNextEntry();
        while (entrada != null) {
            log.debug("Entrada encontrada: " + entrada.getName());

            if (entrada.getName().contains("SELE-SAL-CONSTA")) {
                log.debug("Extrayendo: " + entrada.getName());

                ByteArrayOutputStream fos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];

                while ((bytesRead = zip.read(buffer)) > 0) {
                    fos.write(buffer, 0, bytesRead);
                }

                byte[] result = fos.toByteArray();
                fos.close();

                log.debug("Archivo extraído, tamaño: " + result.length + " bytes");
                return new ArchivoExtraido(result, entrada.getName());
            } else {
            	log.info("No se encontró ninguna entrada que contenga SELE-SAL-CONSTA");
            }
            entrada = zip.getNextEntry();
        }
        zip.close();
        fis.close();
        return null; // Si no se encuentra el archivo
    }
	
}
