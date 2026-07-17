package donaciones.domain.donante;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria para gestionar las entidades Persona.
 * Se encarga de las operaciones de acceso y almacenamiento de datos.
 */
public class RepositorioPersonas {
    private final List<Persona> personas = new ArrayList<>();
    private static long idSequence = 1L;

    public Optional<Persona> buscarPorId(Long id) {
        return personas.stream()
            .filter(p -> p.getId() != null && p.getId().equals(id))
            .findFirst();
    }

    public Optional<Persona> buscarPorEmail(String email) {
        return personas.stream()
            .filter(p -> p.getEmail().equals(email))
            .findFirst();
    }

    public Optional<Persona> buscarPorDocumento(String documento) {
        return personas.stream()
            .filter(p -> p.getDocumento().equals(documento))
            .findFirst();
    }

    public void agregar(Persona persona) {
        if (persona.getId() == null) {
            persona.setId(idSequence++);
        }
        personas.add(persona);
    }

    public void eliminarPorId(Long id) {
        personas.removeIf(p -> p.getId() != null && p.getId().equals(id));
    }

    public void eliminarPorDocumento(String documento) {
        personas.removeIf(p -> p.getDocumento().equals(documento));
    }

    public List<Persona> obtenerTodas() {
        return new ArrayList<>(personas); // Devuelve una copia para proteger la lista interna
    }
}