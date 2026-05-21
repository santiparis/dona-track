import java.util.List;
import java.util.Optional;

public class NecesidadExtra extends Necesidad {
    public NecesidadExtra(EntidadBeneficiaria entidad, String descripcion, List<Bien> bienes) {
        super(entidad, descripcion, bienes);
    }



    @Override
    public void actualizar() {
        return;
    }

    @Override
    public Optional<Necesidad> resolver() {
        return Optional.empty();
    }
}
