package donaciones.domain.cargaCSV;

import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.notificacion.NotificacionPorEmail;
import donaciones.domain.notificacion.NotificacionPorWhatsApp;
import org.apache.commons.csv.CSVRecord;

public class ImportadorPersonas {
    private final RepositorioPersonas repositorioPersonas;

    public ImportadorPersonas(RepositorioPersonas repositorioPersonas) {
        this.repositorioPersonas = repositorioPersonas;
    }

    public void importarPersonasDesdeCSV(String pathArchivo) {
        LectorArchivoCsv lector = new LectorArchivoCsv(pathArchivo);
        List<CSVRecord> registros = lector.leerRegistros();

        List<Persona> donantesPotenciales = registros.stream()
                .map(this::mapearPersona)
                .flatMap(Optional::stream)
                .toList();

        for (Persona personaPotencial : donantesPotenciales) {
            Optional<Persona> personaExistenteOpt = repositorioPersonas.buscarPorEmail(personaPotencial.getEmail());
            if (personaExistenteOpt.isPresent()) {
                Persona personaAActualizar = personaExistenteOpt.get();
                personaAActualizar.actualizarseDesde(personaPotencial);
            } else {
                repositorioPersonas.agregar(personaPotencial);
                Notificacion notificacion = new Notificacion(personaPotencial.getMedioPredeterminado(),
                    "¡Hola " + personaPotencial.getNombre()
                        + "! Te damos la bienvenida a dona-track. Gracias por unirte.");
                notificacion.enviar();
            }
        }
    }

    private Optional<Persona> mapearPersona(CSVRecord record) {
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
        NotificacionPorEmail estrategiaEmail = new NotificacionPorEmail();
        NotificacionPorWhatsApp estrategiaWhatsapp = new NotificacionPorWhatsApp();
        contactos.add(new Contacto(estrategiaEmail, emailStr));
        contactos.add(new Contacto(estrategiaWhatsapp, telefonoStr));
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
