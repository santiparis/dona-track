package donante;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PersonaJuridica extends Persona {
    private final RazonSocial razonSocial;
    private final String rubro;
    private List<PersonaHumana> representantesHabilitados;
    private String direccion;
    private String telefono;
    private List<String> correosRepresentantes;
    private final List<Object> necesidades = new ArrayList<>();

    public PersonaJuridica(
            TipoDoc tipoDoc,
            String documento,
            String nombre,
            RazonSocial razonSocial,
            String rubro,
            List<PersonaHumana> representantesHabilitados,
            List<Contacto> contactos,
            Contacto medioPredeterminado,
            Usuario usuario
    ) {
        super(tipoDoc, documento, nombre, contactos, medioPredeterminado, usuario);
        this.razonSocial = razonSocial;
        this.rubro = rubro;
        this.representantesHabilitados = representantesHabilitados;
    }

    public PersonaJuridica(
            TipoDoc tipoDoc,
            String documento,
            String nombre,
            RazonSocial razonSocial,
            String rubro,
            String direccion,
            String telefono,
            List<String> correosRepresentantes,
            List<PersonaHumana> representantesHabilitados,
            List<Contacto> contactos,
            Contacto medioPredeterminado,
            Usuario usuario
    ) {
        this(tipoDoc, documento, nombre, razonSocial, rubro, representantesHabilitados, contactos, medioPredeterminado, usuario);
        this.direccion = direccion;
        this.telefono = telefono;
        this.correosRepresentantes = correosRepresentantes;
    }

    public RazonSocial getRazonSocial() {
        return razonSocial;
    }

    public String getRubro() {
        return rubro;
    }

    public List<PersonaHumana> getRepresentantesHabilitados() {
        return representantesHabilitados;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public List<String> getCorreosRepresentantes() {
        return correosRepresentantes;
    }

    public void registrarNecesidad(Object sistemaDonaciones, Object necesidad) {
        this.registrarNecesidad(necesidad);
        try {
            Method registrarNecesidad = sistemaDonaciones.getClass().getMethod("registrarNecesidad", necesidad.getClass().getSuperclass());
            registrarNecesidad.invoke(sistemaDonaciones, necesidad);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("No se pudo registrar la necesidad en el sistema", e);
        }
    }

    public void registrarNecesidad(Object necesidad) {
        if (!this.necesidades.contains(necesidad)) {
            this.necesidades.add(necesidad);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getNecesidades() {
        return (List<T>) necesidades;
    }
}
