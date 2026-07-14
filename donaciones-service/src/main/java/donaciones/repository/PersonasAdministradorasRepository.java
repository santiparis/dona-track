package donaciones.repository;

import donaciones.domain.PersonaAdministradora;

import java.util.ArrayList;
import java.util.List;

public class PersonasAdministradorasRepository {

    private static final List<PersonaAdministradora> administradoras = new ArrayList<>();

    public List<PersonaAdministradora> obtenerTodos() {
        return administradoras;
    }

    public void guardar(PersonaAdministradora admin) {
        administradoras.add(admin);
    }
}
