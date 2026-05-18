package Donante;

import java.util.List;

public class PersonaJuridica extends Donante {
    private final RazonSocial razonSocial;
    private final String rubro;
    private List<PersonaHumana> representantesHabilitados;

    public PersonaJuridica(
            TipoDoc tipoDoc,
            String documento,
            String nombre,
            RazonSocial razonSocial,
            String rubro,
            List<PersonaHumana> representantesHabilitados,
            List<TipoContacto> contactos,
            TipoContacto medioPredeterminado
    ) {
        super(tipoDoc, documento, nombre, contactos, medioPredeterminado);
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
}
