package org.santicluke.padronAfip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class ProcesoTest {

	// Instancia de tu clase (reemplaza con el nombre real)
    private final Proceso proceso = new Proceso();

    @Nested
    @DisplayName("Casos exitosos")
    class CasosExitosos {

        @Test
        @DisplayName("Debería extraer fecha válida del formato correcto")
        void deberiaExtraerFechaValidaDelFormatoCorrecto() {
            // Given
            String nombreArchivo = "archivo.20250127.tmp";
            LocalDate fechaEsperada = LocalDate.of(2025, 1, 27);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }

        @Test
        @DisplayName("Debería funcionar con nombres de archivo largos")
        void deberiaFuncionarConNombresDeArchivoLargos() {
            // Given
            String nombreArchivo = "mi_archivo_muy_largo_con_muchos_caracteres.20231215.tmp";
            LocalDate fechaEsperada = LocalDate.of(2023, 12, 15);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }

        @Test
        @DisplayName("Debería funcionar con rutas completas")
        void deberiaFuncionarConRutasCompletas() {
            // Given
            String nombreArchivo = "/ruta/completa/archivo.20220301.tmp";
            LocalDate fechaEsperada = LocalDate.of(2022, 3, 1);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }

        @Test
        @DisplayName("Debería funcionar con fechas límite")
        void deberiaFuncionarConFechasLimite() {
            // Given - Año bisiesto
            String nombreArchivo = "archivo.20240229.tmp";
            LocalDate fechaEsperada = LocalDate.of(2024, 2, 29);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }

        @ParameterizedTest
        @DisplayName("Debería funcionar con diferentes fechas válidas")
        @ValueSource(strings = {
            "test.20250101.tmp", // Año nuevo
            "data.20251231.tmp", // Fin de año
            "file.20240229.tmp", // Año bisiesto
            "backup.19991231.tmp", // Siglo pasado
            "export.20300615.tmp"  // Futuro
        })
        void deberiaFuncionarConDiferentesFechasValidas(String nombreArchivo) {
            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertNotNull(resultado);
        }
    }

    @Nested
    @DisplayName("Casos que retornan null")
    class CasosQueRetornanNull {

        @ParameterizedTest
        @DisplayName("Debería retornar null para entradas nulas o vacías")
        @NullAndEmptySource
        void deberiaRetornarNullParaEntradasNulasOVacias(String nombreArchivo) {
            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertNull(resultado);
        }

        @Test
        @DisplayName("Debería retornar null si no encuentra el patrón")
        void deberiaRetornarNullSiNoEncuentraElPatron() {
            // Given
            String nombreArchivo = "archivo_sin_fecha.txt";

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertNull(resultado);
        }

        @ParameterizedTest
        @DisplayName("Debería retornar null para formatos incorrectos")
        @ValueSource(strings = {
            "archivo.2025127.tmp",      // 7 dígitos
            "archivo.202501271.tmp",    // 9 dígitos
            "archivo.20250127",         // Sin .tmp
            "archivo20250127.tmp",      // Sin punto antes de fecha
            "archivo.20250127.txt",     // Extensión incorrecta
            "archivo.20250127.tmp.bak", // Caracteres después de .tmp
            "archivo.abcd1234.tmp",     // Caracteres no numéricos
            "archivo.2025abcd.tmp"      // Mezcla de números y letras
        })
        void deberiaRetornarNullParaFormatosIncorrectos(String nombreArchivo) {
            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertNull(resultado);
        }

        @ParameterizedTest
        @DisplayName("Debería retornar null para fechas inválidas")
        @ValueSource(strings = {
            "archivo.20251301.tmp", // Mes 13
            "archivo.20250001.tmp", // Mes 00
            "archivo.20250100.tmp", // Día 00
            "archivo.20250132.tmp", // Día 32
            "archivo.00000101.tmp", // Año 0000
        })
        void deberiaRetornarNullParaFechasInvalidas(String nombreArchivo) {
            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertNull(resultado);
        }
    }

    @Nested
    @DisplayName("Casos edge")
    class CasosEdge {

        @Test
        @DisplayName("Debería manejar múltiples patrones y tomar el último")
        void deberiaManejarMultiplesPlatronesYTomarElUltimo() {
            // Given - Solo debería tomar el último patrón que coincida
            String nombreArchivo = "archivo.20240101.backup.20250127.tmp";
            LocalDate fechaEsperada = LocalDate.of(2025, 1, 27);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }

        @Test
        @DisplayName("Debería manejar string muy largo")
        void deberiaManejarStringMuyLargo() {
            // Given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("a");
            }
            sb.append(".20250127.tmp");
            String nombreArchivo = sb.toString();
            LocalDate fechaEsperada = LocalDate.of(2025, 1, 27);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }

        @Test
        @DisplayName("Debería ser case sensitive")
        void deberiaSercaseSensitive() {
            // Given
            String nombreArchivo = "archivo.20250127.TMP"; // TMP en mayúsculas

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertNull(resultado); // Debería ser null porque busca .tmp en minúsculas
        }

        @Test
        @DisplayName("Debería manejar caracteres especiales en el nombre")
        void deberiaManejarCaracteresEspecialesEnElNombre() {
            // Given
            String nombreArchivo = "archivo-con_símbolos$%&.20250127.tmp";
            LocalDate fechaEsperada = LocalDate.of(2025, 1, 27);

            // When
            LocalDate resultado = proceso.obtenerFechaDeArchivo(nombreArchivo);

            // Then
            assertEquals(fechaEsperada, resultado);
        }
    }

    @Nested
    @DisplayName("Tests de rendimiento básico")
    class TestsDeRendimiento {

        @Test
        @DisplayName("Debería procesar muchos archivos rápidamente")
        void deberiaProcsearMuchosArchivosRapidamente() {
            // Given
            String[] archivos = new String[10000];
            for (int i = 0; i < archivos.length; i++) {
                archivos[i] = "archivo" + i + ".20250127.tmp";
            }

            // When
            long inicio = System.currentTimeMillis();
            for (String archivo : archivos) {
                proceso.obtenerFechaDeArchivo(archivo);
            }
            long duracion = System.currentTimeMillis() - inicio;

            // Then
            assertTrue(duracion < 1000, "Debería procesar 10k archivos en menos de 1 segundo");
        }
    }
	
}
