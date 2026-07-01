package donaciones.domain.cargaCSV;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

/**
 * Responsable exclusivamente de leer un archivo CSV desde el classpath
 * y devolver sus registros crudos como {@link CSVRecord}.
 * No conoce el dominio ni realiza transformaciones de negocio.
 */
public class LectorArchivoCsv {

  private final String pathArchivo;

  public LectorArchivoCsv(String pathArchivo) {
    this.pathArchivo = Objects.requireNonNull(pathArchivo, "El pathArchivo no puede ser null");
  }

  public List<CSVRecord> leerRegistros() {
    try (InputStream is = getInputStream();
        BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(is).get();
        Reader reader = new InputStreamReader(bomInputStream, StandardCharsets.UTF_8);
        CSVParser csvParser = CSVParser.parse(reader, CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader().get())) {

      return csvParser.getRecords();

    } catch (IOException e) {
      throw new RuntimeException("Error de I/O al procesar el archivo CSV: " + pathArchivo, e);
    }
  }

  private InputStream getInputStream() {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(this.pathArchivo);
    if (inputStream == null) {
      throw new IllegalArgumentException(
          "No se pudo encontrar el archivo en resources: " + this.pathArchivo);
    }
    return inputStream;
  }
}
