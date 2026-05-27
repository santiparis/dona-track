package cargaCSV;

import donante.*;
import donante.RepositorioPersonas;

import java.util.List;
import java.util.Optional;

public class ImportadorPersonas {
    private final RepositorioPersonas repositorioPersonas;
    private LectorArchivoCsv lectorArchivoCsv;

    public ImportadorPersonas(RepositorioPersonas repositorioPersonas) {
        this.repositorioPersonas = repositorioPersonas;
    }

    public void importarPersonasDesdeCSV(String pathArchivo){
        lectorArchivoCsv = new LectorArchivoCsv(pathArchivo);
        List<Persona> donantesPotenciales = lectorArchivoCsv.procesarCsv();
        for (Persona personaPotencial : donantesPotenciales) {
            Optional<Persona> personaExistenteOpt = repositorioPersonas.buscarPorEmail(personaPotencial.getEmail());
            if (personaExistenteOpt.isPresent()) {
                Persona personaAActualizar = personaExistenteOpt.get();
                personaAActualizar.actualizarseDesde(personaPotencial);
            } else {
                repositorioPersonas.agregar(personaPotencial);
                personaPotencial.enviarEmailDeBienvenida();
            }
        }
    }
}
