import donante.Persona;
import cargaCSV.ImportadorDonantesCSV;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import notificacion.ServicioDeNotificacion;

public class Aplicacion {
    private final List<Persona> personas;
    private final ServicioDeNotificacion servicioDeNotificacion;

  public Aplicacion() {
        this.personas = new ArrayList<>();
        this.servicioDeNotificacion = new ServicioDeNotificacion();
    }

  public void agregarPersona(Persona persona){
      personas.add(persona);
      servicioDeNotificacion.enviarEmailDeBienvenida(persona);
  }

  public List<Persona> getPersonas() {
      return personas;
  }

  public Optional<Persona> buscarPersonaPorEmail(String email) {
      return personas.stream()
              .filter(d -> d.getEmail().equals(email))
              .findFirst();
  }

  public void importarPersonasDesdeCSV(String pathArchivo) throws IOException {
    ImportadorDonantesCSV importadorDonantesCSV = new ImportadorDonantesCSV(pathArchivo);
      List<Persona> donantesPotenciales = importadorDonantesCSV.procesar();
      for (Persona personaPotencial : donantesPotenciales) {
          Optional<Persona> personaExistenteOpt = buscarPersonaPorEmail(personaPotencial.getEmail());
          if (personaExistenteOpt.isPresent()) {
            Persona personaAActualizar = personaExistenteOpt.get();
            personaAActualizar.actualizarseDesde(personaPotencial);
          } else {
              agregarPersona(personaPotencial);
          }
      }
  }

  public Optional<Persona> buscarPersonaPorDocumento(String documento) {
    return personas.stream()
        .filter(d -> d.getDocumento().equals(documento))
        .findFirst();
  }

  public void actualizarPersona(String documento, Persona personaConNuevosDatos) {
    buscarPersonaPorDocumento(documento)
        .ifPresent(existente -> existente.actualizarseDesde(personaConNuevosDatos));
  }

  public void eliminarPersona(String documento) {
    personas.removeIf(d -> d.getDocumento().equals(documento));
  }
}