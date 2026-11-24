package donaciones.domain.cargaCSV;

import donaciones.domain.notificacion.Contacto;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import donaciones.domain.donante.Rol;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.ContactoPorWhatsApp;
import org.apache.commons.csv.CSVRecord;

public class ImportadorPersonas {
    private static final int TANDA_DE_EMAILS = 1000;
    private final RepositorioPersonas repositorioPersonas;

    public ImportadorPersonas(RepositorioPersonas repositorioPersonas) {
        this.repositorioPersonas = repositorioPersonas;
    }

    public ResultadoImportacion importarPersonasDesdeCSV(InputStream archivo) {
        LectorArchivoCsv lector = new LectorArchivoCsv(archivo);
        List<CSVRecord> registros = lector.leerRegistros();

        List<Persona> donantesPotenciales = registros.stream()
                .map(this::mapearPersona)
                .flatMap(Optional::stream)
                .toList();

        Map<String, Persona> existentes = buscarExistentes(donantesPotenciales);
        int creados = 0;
        for (Persona personaPotencial : donantesPotenciales) {
            Persona personaExistente = existentes.get(personaPotencial.getEmail());
            if (personaExistente != null) {
                personaExistente.actualizarseDesde(personaPotencial);
            } else {
                repositorioPersonas.agregar(personaPotencial);
                existentes.put(personaPotencial.getEmail(), personaPotencial); // mail repetido en el archivo
                creados++;
            }
        }
        return new ResultadoImportacion(registros.size(), creados, donantesPotenciales.size() - creados);
    }

    // Una consulta cada TANDA_DE_EMAILS mails, en vez de una por fila
    private Map<String, Persona> buscarExistentes(List<Persona> personas) {
        List<String> emails = personas.stream().map(Persona::getEmail).toList();
        Map<String, Persona> existentes = new HashMap<>();
        for (int desde = 0; desde < emails.size(); desde += TANDA_DE_EMAILS) {
            List<String> tanda = emails.subList(desde, Math.min(desde + TANDA_DE_EMAILS, emails.size()));
            repositorioPersonas.buscarPorEmails(tanda).forEach(persona -> existentes.put(persona.getEmail(), persona));
        }
        return existentes;
    }

    private Optional<Persona> mapearPersona(CSVRecord record) {
        try {
            String tipoPersona = record.get("TipoPersona");
            String emailStr = record.get("Email");
            String telefonoStr = record.get("Teléfono");
            String nombreCompleto = record.get("Nombre/Razón Social");
            TipoDoc tipoDoc = TipoDoc.valueOf(record.get("TipoDoc"));
            String documento = record.get("Documento");
            List<Contacto> contactos = crearContactos(emailStr, telefonoStr);
            Contacto medioPredeterminado = contactos.get(0);
            Usuario nuevoUsuario = new Usuario(emailStr, "password_provisoria", Rol.DONANTE);

            Persona persona;
            if ("HUMANA".equalsIgnoreCase(tipoPersona)) {
                String[] nombreYApellido = separarNombreYApellido(nombreCompleto);
                persona = new PersonaHumana(
                        nombreYApellido[0],
                        nombreYApellido[1],
                        null, tipoDoc, documento, null, null,
                        contactos, medioPredeterminado, nuevoUsuario);
            } else if ("JURIDICA".equalsIgnoreCase(tipoPersona)) {
                persona = new PersonaJuridica(
                        tipoDoc, documento, nombreCompleto, null, null,
                        new ArrayList<>(), contactos, medioPredeterminado, nuevoUsuario);
            } else {
                return Optional.empty();
            }
            return Optional.of(persona);

        } catch (IllegalArgumentException e) {
            throw new MapeoCsvEnPersonaException("Error mapeando registro: " + record + e);
        }
    }

    private List<Contacto> crearContactos(String emailStr, String telefonoStr) {
        List<Contacto> contactos = new ArrayList<>();
        contactos.add(new ContactoPorEmail(emailStr));
        contactos.add(new ContactoPorWhatsApp(telefonoStr));
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
        return new String[] { nombre, apellido };
    }
}
