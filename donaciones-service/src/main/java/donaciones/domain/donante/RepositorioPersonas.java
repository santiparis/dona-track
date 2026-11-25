package donaciones.domain.donante;

import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Repositorio JPA de personas. */
public class RepositorioPersonas implements WithSimplePersistenceUnit {
    public Optional<Persona> buscarPorUsuario(Long usuarioId) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from Persona p where p.usuario.id = :usuarioId", Persona.class)
                .setParameter("usuarioId", usuarioId)
                .getResultStream()
                .findFirst();
    }


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

    public Optional<Persona> buscarPorEmail(String email) {
        return createQuery("select p from Persona p join p.contactos c where c.valor = :email", Persona.class)
            .setParameter("email", email)
            .getResultStream()
            .findFirst();
    }

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

    // Trae también el usuario y todos los contactos para no hacer una consulta más por persona
    public List<Persona> buscarPorEmails(List<String> emails) {
        return createQuery("select distinct p from Persona p left join fetch p.usuario join fetch p.contactos "
                + "join p.contactos c where c.valor in :emails", Persona.class)
            .setParameter("emails", emails)
            .getResultList();
    }

    public List<Persona> buscarInactivasDesde(LocalDateTime umbral) {
        return createQuery("from Persona p where p.ultimaInteraccion < :umbral", Persona.class)
            .setParameter("umbral", umbral)
            .getResultList();
    }
}
