package donante;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria para gestionar las entidades Persona.
 * Se encarga de las operaciones de acceso y almacenamiento de datos.
 */
public class RepositorioPersonas {
    private final List<Persona> personas = new ArrayList<>();

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
        personas.add(persona);
    }

    public void eliminarPorDocumento(String documento) {
        personas.removeIf(p -> p.getDocumento().equals(documento));
    }

    public List<Persona> obtenerTodas() {
        return new ArrayList<>(personas); // Devuelve una copia para proteger la lista interna
    }
}