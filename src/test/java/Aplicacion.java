import donante.Persona;
import cargaCSV.ImportadorDonantesCSV;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import notificacion.ServicioDeNotificacion;

public class Aplicacion {
    private final List<Persona> personas;

  public Aplicacion() {
        this.personas = new ArrayList<>();
    }

  public void agregarDonante(Persona persona){
      personas.add(persona);
  }

  public List<Persona> getDonantes() {
      return personas;
  }

  public Optional<Persona> buscarDonantePorEmail(String email) {
      return personas.stream()
              .filter(d -> d.getEmail().equals(email))
              .findFirst();
  }

  public void importarDonantesDesdeCSV(String pathArchivo) throws IOException {
    ImportadorDonantesCSV importadorDonantesCSV = new ImportadorDonantesCSV(pathArchivo);
      List<Persona> donantesPotenciales = importadorDonantesCSV.procesar();
      for (Persona persona : donantesPotenciales) {
          Optional<Persona> donanteExistente = buscarDonantePorEmail(persona.getEmail());
          if (donanteExistente.isPresent()) {
            Persona personaAActualizar = donanteExistente.get();
            personaAActualizar.actualizarDatos(persona);
          } else {
              personas.add(persona);
          }
      }
  }
}