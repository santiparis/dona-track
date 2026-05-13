public enum Subcategoria {
    BANCOS_COLEGIO(Categoria.MUEBLE, true, false),
    PAQUETE_FIDEOS(Categoria.ALIMENTO, false, false);

    private final Categoria categoria;
    private final boolean requiereEstado;
    private final boolean requiereVencimiento;

    Subcategoria(
            Categoria categoria,
            boolean requiereEstado,
            boolean requiereVencimiento
    ) {
        this.categoria = categoria;
        this.requiereEstado = requiereEstado;
        this.requiereVencimiento = requiereVencimiento;
    }

    public Categoria getCategoria() {
        return this.categoria;
    }

    public boolean requiereEstado() {
        return this.requiereEstado;
    }

    public boolean requiereVencimiento() {
        return this.requiereVencimiento;
    }
}
