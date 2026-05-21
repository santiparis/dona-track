import donante.Donante;
import cargaCSV.ImportadorDonantesCSV;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Aplicacion {
    private final List<Donante> donantes;

  public Aplicacion() {
        this.donantes = new ArrayList<>();
    }

  public void agregarDonante(Donante donante){
      donantes.add(donante);
  }

  public List<Donante> getDonantes() {
      return donantes;
  }

  public Optional<Donante> buscarDonantePorEmail(String email) {
      return donantes.stream()
              .filter(d -> d.getEmail().equals(email))
              .findFirst();
  }

  public void importarDonantesDesdeCSV(String pathArchivo) throws IOException {
    ImportadorDonantesCSV importadorDonantesCSV = new ImportadorDonantesCSV(pathArchivo);
      List<Donante> donantesPotenciales = importadorDonantesCSV.procesar();
      for (Donante donante : donantesPotenciales) {
          Optional<Donante> donanteExistente = buscarDonantePorEmail(donante.getEmail());
          if (donanteExistente.isPresent()) {
            Donante donanteAActualizar = donanteExistente.get();
            donanteAActualizar.actualizarDatos(donante);
          } else {
              donantes.add(donante);
          }
      }
  }
}