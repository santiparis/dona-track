package donaciones.domain;

import donaciones.domain.donante.Contacto;
import donaciones.domain.notificacion.Notificable;
import java.util.ArrayList;
import java.util.List;

public class PersonaAdministradora implements Notificable {
    private final String nombre;
    private final List<Contacto> contactos = new ArrayList<>();
    private Contacto medioPredeterminado;

    public PersonaAdministradora(String nombre, List<Contacto> contactos, Contacto medioPredeterminado) {
        this.nombre = nombre;
        if (contactos != null) {
            this.contactos.addAll(contactos);
        }
        this.medioPredeterminado = medioPredeterminado;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public List<Contacto> getContactos() {
        return contactos;
    }

    @Override
    public Contacto getMedioPredeterminado() {
        return medioPredeterminado;
    }

    // TODO: Implementar confirmación de destino final de donaciones tras la ejecución de los algoritmos de matchmaking.
    // TODO: Implementar revisión y gestión de entregas no satisfactorias (para decidir replanificación o regreso a depósito).
    // TODO: Implementar acceso y visualización del dashboard de monitoreo de camiones en tiempo real.
}
