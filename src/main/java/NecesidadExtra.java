public class NecesidadExtra extends Necesidad {
    public NecesidadExtra(EntidadBeneficiaria entidad, Subcategoria subcategoria, Integer cantidad, String descripcion) {
        super(entidad, subcategoria, cantidad, descripcion);
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
