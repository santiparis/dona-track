package cargaCSV;

import donante.*;
import donante.RepositorioPersonas;
import notificacion.ServicioDeNotificacion;

import java.util.List;
import java.util.Optional;

public class ImportadorPersonas {
    private final RepositorioPersonas repositorioPersonas;
    private final ServicioDeNotificacion servicioDeNotificacion;
    private LectorArchivoCsv lectorArchivoCsv;

    public ImportadorPersonas(RepositorioPersonas repositorioPersonas, ServicioDeNotificacion servicioDeNotificacion) {
        this.repositorioPersonas = repositorioPersonas;
        this.servicioDeNotificacion = servicioDeNotificacion;
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
//                servicioDeNotificacion.enviarEmailDeBienvenida(personaPotencial);
            }
        }
    }
}
