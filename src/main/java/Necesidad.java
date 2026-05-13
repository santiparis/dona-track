abstract class Necesidad {
    private final EntidadBeneficiaria entidad;
    private final Subcategoria subcategoria;
    private final Integer cantidad;
    private final String descripcion;

    public Necesidad(
            EntidadBeneficiaria entidad,
            Subcategoria subcategoria,
            Integer cantidad,
            String descripcion
    ) {
        this.entidad = entidad;
        this.subcategoria = subcategoria;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
    }

    public EntidadBeneficiaria getEntidad() {
        return this.entidad;
    }

    public Subcategoria getSubcategoria() {
        return this.subcategoria;
    }

    public Integer getCantidad() {
        return this.cantidad;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void actualizar() {}

    public void resolver() {}
}
