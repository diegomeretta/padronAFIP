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

@Component
public class ZipFileManager {
	
	public ArchivoExtraido obtenerArchivoSituacionFiscalDesdeAFIPWeb(String afipFileUrl, Date fileLastModified) {

		try {
            URI downloadedFileUri = downloadAfipZipFile(afipFileUrl, fileLastModified);
            Path filePath = Paths.get(downloadedFileUri);
            System.out.println("filePath:" + filePath.toString());
            ArchivoExtraido extractedContent = extraerArchivoDeZipDesdeArchivo(filePath);
            // Files.deleteIfExists(filePath);
            // System.out.println("Archivo temporal eliminado: " + filePath);
            return extractedContent;
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
        	e.printStackTrace();
        }
        return null;
	}

    public URI downloadAfipZipFile(String afipFileUrl, Date fileLastModified) throws MalformedURLException, IOException, URISyntaxException {
        System.out.println("Downloading ZIP from: " + afipFileUrl);
        
        Path downloadDir = Paths.get("downloads");
        Files.createDirectories(downloadDir);
        
        String fileName = generateFileName(afipFileUrl, fileLastModified);
        Path filePath = downloadDir.resolve(fileName);
        
        URL url = new URI(afipFileUrl).toURL();
        try (InputStream inputStream = url.openStream()) {
            long bytesDownloaded = Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Download completed. File saved: " + filePath);
            System.out.println("Total bytes downloaded: " + bytesDownloaded);
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
        System.out.println("Extracting ZIP from file: " + zipFilePath);
        long fileSize = Files.size(zipFilePath);
        System.out.println("Tamaño del archivo: " + fileSize + " bytes");
        if (fileSize == 0) {
            System.err.println("ERROR: El archivo está vacío");
        }
        System.out.println("Permisos de lectura: " + Files.isReadable(zipFilePath));
        
        FileInputStream fis = new FileInputStream(zipFilePath.toFile());
        byte[] header = new byte[4];
        int bytesRead = fis.read(header);
        if (bytesRead >= 2) {
            if (header[0] != 0x50 || header[1] != 0x4B) {
                System.err.println("ERROR: No es un archivo ZIP válido (esperado: 0x504B)");
            }
        }
        
        ZipInputStream zip = new ZipInputStream(fis);
    	System.out.println("Procesando archivo zip");
    	ZipEntry entrada = zip.getNextEntry();
        while (entrada != null) {
            System.out.println("Entrada encontrada: " + entrada.getName());

            if (entrada.getName().contains("SELE-SAL-CONSTA")) {
                System.out.println("Extrayendo: " + entrada.getName());

                ByteArrayOutputStream fos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];

                while ((bytesRead = zip.read(buffer)) > 0) {
                    fos.write(buffer, 0, bytesRead);
                }

                byte[] result = fos.toByteArray();
                fos.close();

                System.out.println("Archivo extraído, tamaño: " + result.length + " bytes");
                return new ArchivoExtraido(result, entrada.getName());
            } else {
            	System.out.println("No se encontró ninguna entrada que contenga SELE-SAL-CONSTA");
            }
            entrada = zip.getNextEntry();
        }
        zip.close();
        return null; // Si no se encuentra el archivo
    }
	
}
