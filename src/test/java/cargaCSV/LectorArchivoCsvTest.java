package cargaCSV;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LectorArchivoCsvTest {

    private static List<CSVRecord> registros;

    @BeforeAll
    static void setUp() {
        String pathCsv = "donantes_import_20000_UTF8_BOM.csv";
        LectorArchivoCsv lectorArchivoCsv = new LectorArchivoCsv(pathCsv);
        registros = lectorArchivoCsv.leerRegistros();
    }

    @Test
    void leerRegistrosDevuelveLaCantidadCorrectaDeFilas() {
        assertFalse(registros.isEmpty(), "La lista de registros no debería estar vacía.");
        assertEquals(20000, registros.size(), "Debería haber 20,000 registros en el CSV.");
    }

    @Test
    void losRegistrosContienenLasColumnasEsperadas() {
        CSVRecord primerRegistro = registros.get(0);

        assertTrue(primerRegistro.isMapped("TipoPersona"), "Debería contener la columna 'TipoPersona'.");
        assertTrue(primerRegistro.isMapped("Email"), "Debería contener la columna 'Email'.");
        assertTrue(primerRegistro.isMapped("Teléfono"), "Debería contener la columna 'Teléfono'.");
        assertTrue(primerRegistro.isMapped("Nombre/Razón Social"), "Debería contener la columna 'Nombre/Razón Social'.");
        assertTrue(primerRegistro.isMapped("Donante.TipoDoc"), "Debería contener la columna 'Donante.TipoDoc'.");
        assertTrue(primerRegistro.isMapped("Documento"), "Debería contener la columna 'Documento'.");
    }
}