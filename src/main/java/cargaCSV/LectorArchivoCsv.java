package cargaCSV;

import donante.Contacto;
import donante.Persona;
import donante.PersonaHumana;
import donante.PersonaJuridica;
import donante.TipoContacto;
import donante.TipoDoc;
import donante.Usuario;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

public class LectorArchivoCsv {

  private final String pathArchivo;

  public LectorArchivoCsv(String pathArchivo){
    this.pathArchivo = Objects.requireNonNull(pathArchivo, "El pathArchivo no puede ser null");
  }

  public List<Persona> procesarCsv() {
    try (InputStream is = getInputStream();
        BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(is).get();
        Reader reader = new InputStreamReader(bomInputStream, StandardCharsets.UTF_8);
        CSVParser csvParser = CSVParser.parse(reader, CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader().get())) {

      return csvParser.getRecords().stream()
          .map(this::mapearDonante)
          .flatMap(Optional::stream)
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new RuntimeException("Error de I/O al procesar el archivo CSV: " + pathArchivo, e);
    }
  }

  private InputStream getInputStream() {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(this.pathArchivo);
    if (inputStream == null) {
      throw new IllegalArgumentException(
          "No se pudo encontrar el archivo en resources: " + this.pathArchivo
      );
    }
    return inputStream;
  }

  private Optional<Persona> mapearDonante(CSVRecord record) {
    try {
      String tipoPersona = record.get("TipoPersona");
      String emailStr = record.get("Email");
      String telefonoStr = record.get("Teléfono");
      String nombreCompleto = record.get("Nombre/Razón Social");
      TipoDoc tipoDoc = TipoDoc.valueOf(record.get("Donante.TipoDoc"));
      String documento = record.get("Documento");
      List<Contacto> contactos = crearContactos(emailStr, telefonoStr);
      Contacto medioPredeterminado = contactos.get(0);
      Usuario nuevoUsuario = new Usuario(emailStr, "password_provisoria");

      Persona persona;
      if ("HUMANA".equalsIgnoreCase(tipoPersona)) {
        String[] nombreYApellido = separarNombreYApellido(nombreCompleto);
        persona = new PersonaHumana(
            nombreYApellido[0],
            nombreYApellido[1],
            null, tipoDoc, documento, null, null,
            contactos, medioPredeterminado, nuevoUsuario
        );
      } else if ("JURIDICA".equalsIgnoreCase(tipoPersona)) {
        persona = new PersonaJuridica(
            tipoDoc, documento, nombreCompleto, null, null,
            new ArrayList<>(), contactos, medioPredeterminado, nuevoUsuario
        );
      } else {
        return Optional.empty();
      }
      return Optional.of(persona);

    } catch (IllegalArgumentException e) {
      // Capturamos el error específico de conversión y lo envolvemos en nuestra excepción
      throw new MapeoCsvEnDonanteException("Error mapeando registro: " + record + e);
    }
  }

  private List<Contacto> crearContactos(String emailStr, String telefonoStr) {
    List<Contacto> contactos = new ArrayList<>();
    contactos.add(new Contacto(TipoContacto.EMAIL, emailStr));
    contactos.add(new Contacto(TipoContacto.WHATSAPP, telefonoStr));
    return contactos;
  }

  private String[] separarNombreYApellido(String nombreCompleto) {
    String nombre;
    String apellido = null;
    int primerEspacio = nombreCompleto.indexOf(' ');
    if (primerEspacio != -1) {
      nombre = nombreCompleto.substring(0, primerEspacio);
      apellido = nombreCompleto.substring(primerEspacio + 1);
    } else {
      nombre = nombreCompleto;
    }
    return new String[]{nombre, apellido};
  }
}
