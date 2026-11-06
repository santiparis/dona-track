package donaciones.domain.donante;

import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;
import java.util.Optional;

/** Repositorio JPA de personas. */
public class RepositorioPersonas implements WithSimplePersistenceUnit {
    public Optional<Persona> buscarPorId(Long id) {
        return Optional.ofNullable(find(Persona.class, id));
    }

    public Optional<Persona> buscarPorIdParaActualizar(Long id) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return Optional.ofNullable(find(Persona.class, id));
    }

    public Optional<Persona> buscarPorIdParaReferencia(Long id) {
        return Optional.ofNullable(find(Persona.class, id));
    }

    public Optional<Persona> buscarPorEmail(String email) { return obtenerTodas().stream().filter(p -> email.equals(p.getEmail())).findFirst(); }

    public Optional<Persona> buscarPorDocumento(String documento) {
        return createQuery("from Persona p where p.documento = :documento", Persona.class)
            .setParameter("documento", documento)
            .getResultStream()
            .findFirst();
    }

    public void agregar(Persona persona) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        if (persona.getId() == null) persist(persona);
    }

    public void eliminarPorId(Long id) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        buscarPorIdParaActualizar(id).ifPresent(this::remove);
    }

    public void eliminarPorDocumento(String documento) { buscarPorDocumento(documento).ifPresent(p -> eliminarPorId(p.getId())); }

    public List<Persona> obtenerTodas() {
        return createQuery("from Persona", Persona.class).getResultList();
    }
}
