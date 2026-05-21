import java.util.List;

public class NecesidadExtra extends Necesidad {
    public NecesidadExtra(EntidadBeneficiaria entidad, String descripcion, List<Bien> bienes) {
        super(entidad, descripcion, bienes);
    }

    @Override
    public void actualizar() {
        return;
    }

    @Override
    public void resolver() {
        return;
    }
}
