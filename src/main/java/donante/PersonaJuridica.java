package donante;

import java.util.List;

public class PersonaJuridica extends Persona {
    private final RazonSocial razonSocial;
    private String rubro;
    private List<PersonaHumana> representantesHabilitados;

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

    public RazonSocial getRazonSocial() {
        return razonSocial;
    }

    public String getRubro() {
        return rubro;
    }

    public List<PersonaHumana> getRepresentantesHabilitados() {
        return representantesHabilitados;
    }

    @Override
    public void actualizarseDesde(Persona personaConNuevosDatos) {
        if (personaConNuevosDatos instanceof PersonaJuridica nuevosDatos) {
            super.actualizarDatosComunes(nuevosDatos);
            this.rubro = nuevosDatos.getRubro();
            this.representantesHabilitados = nuevosDatos.getRepresentantesHabilitados();
        } else {
            throw new IllegalArgumentException("Incompatibilidad de tipos: no se puede actualizar una PersonaJuridica con datos de " + personaConNuevosDatos.getClass().getSimpleName());
        }
    }
}
