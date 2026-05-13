public class DonacionIndependiente {
    private final Subcategoria subcategoria;
    private final Integer cantidad;
    private final String unidad;
    private EstadoDonacion estado = EstadoDonacion.EN_DEPOSITO;

    public DonacionIndependiente(
        Subcategoria subcategoria,
        Integer cantidad,
        String unidad
    ) {
        this.subcategoria = subcategoria;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }

    public Subcategoria getSubcategoria() {
        return this.subcategoria;
    }

    public Integer getCantidad() {
        return this.cantidad;
    }

    public String getUnidad() {
        return this.unidad;
    }

    public EstadoDonacion getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoDonacion estado) {
        this.estado = estado;
    }
}
