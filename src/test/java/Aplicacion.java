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
      for (Donante donantePotencial : donantesPotenciales) {
          Optional<Donante> donanteExistenteOpt = buscarDonantePorEmail(donantePotencial.getEmail());
          if (donanteExistenteOpt.isPresent()) {
            Donante donanteAActualizar = donanteExistenteOpt.get();
            donanteAActualizar.actualizarseDesde(donantePotencial);
          } else {
              donantes.add(donantePotencial);
          }
      }
  }

  public Optional<Donante> buscarDonantePorDocumento(String documento) {
    return donantes.stream()
        .filter(d -> d.getDocumento().equals(documento))
        .findFirst();
  }

  public void actualizarDonante(String documento, Donante donanteConNuevosDatos) {
    buscarDonantePorDocumento(documento)
        .ifPresent(existente -> existente.actualizarseDesde(donanteConNuevosDatos));
  }

  public void eliminarDonante(String documento) {
    donantes.removeIf(d -> d.getDocumento().equals(documento));
  }
}