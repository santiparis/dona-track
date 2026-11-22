package donaciones.domain.cargaCSV;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

/**
 * Responsable exclusivamente de leer un archivo CSV (el que se sube desde la web)
 * y devolver sus registros crudos como {@link CSVRecord}.
 * No conoce el dominio ni realiza transformaciones de negocio.
 */
public class LectorArchivoCsv {

  private final InputStream archivo;

  public LectorArchivoCsv(InputStream archivo) {
    this.archivo = archivo;
  }

  public List<CSVRecord> leerRegistros() {
    try (BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(archivo).get();
        Reader reader = new InputStreamReader(bomInputStream, StandardCharsets.UTF_8);
        CSVParser csvParser = CSVParser.parse(reader, CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader().get())) {

      return csvParser.getRecords();

    } catch (IOException e) {
      throw new RuntimeException("Error de I/O al procesar el archivo CSV", e);
    }
  }
}
