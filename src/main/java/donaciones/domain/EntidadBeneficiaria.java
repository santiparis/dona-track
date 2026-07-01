package donaciones.domain;

import donante.Contacto;
import notificacion.Notificable;
import java.util.ArrayList;
import java.util.List;

public class EntidadBeneficiaria implements Notificable {
    private final String razonSocial;
    private final String direccion;
    private final String telefono;
    private final List<String> correosRepresentantes;
    private final List<Necesidad> necesidades = new ArrayList<>();
    private final List<Contacto> contactos = new ArrayList<>();
    private Contacto medioPredeterminado;

    public EntidadBeneficiaria(
            String razonSocial,
            String direccion,
            String telefono,
            List<String> correosRepresentantes
    ) {
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correosRepresentantes = correosRepresentantes;
    }

    public EntidadBeneficiaria(
            String razonSocial,
            String direccion,
            String telefono,
            List<String> correosRepresentantes,
            List<Contacto> contactos,
            Contacto medioPredeterminado
    ) {
        this(razonSocial, direccion, telefono, correosRepresentantes);
        if (contactos != null) {
            this.contactos.addAll(contactos);
        }
        this.medioPredeterminado = medioPredeterminado;
    }

    public String getRazonSocial() {
        return this.razonSocial;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public String getTelefono() {
        return this.telefono;
    }

    public List<String> getCorreosRepresentantes() {
        return this.correosRepresentantes;
    }

    public void registrarNecesidad(Necesidad necesidad) {
        if (!this.necesidades.contains(necesidad)) {
            this.necesidades.add(necesidad);
        }
    }

    public List<Necesidad> getNecesidades() {
        return necesidades;
    }

    @Override
    public List<Contacto> getContactos() {
        return this.contactos;
    }

    @Override
    public Contacto getMedioPredeterminado() {
        return this.medioPredeterminado;
    }

    public void registrarContacto(Contacto contacto, boolean predeterminado) {
        if (!this.contactos.contains(contacto)) {
            this.contactos.add(contacto);
        }
        if (predeterminado || this.medioPredeterminado == null) {
            this.medioPredeterminado = contacto;
        }
    }
}
