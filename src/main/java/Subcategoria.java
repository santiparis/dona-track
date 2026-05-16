public enum Subcategoria {
    BANCOS(Categoria.MUEBLE, true, false,"Bancos"),
    FIDEOS(Categoria.ALIMENTO, false, false,"Fideos");

    private final Categoria categoria;
    private final boolean requiereEstado;
    private final boolean requiereVencimiento;
    private final String nombre;

    Subcategoria(
            Categoria categoria,
            boolean requiereEstado,
            boolean requiereVencimiento,
            String nombre
    ) {
        this.categoria = categoria;
        this.requiereEstado = requiereEstado;
        this.requiereVencimiento = requiereVencimiento;
        this.nombre = nombre;
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
    public String nombre(){
      return this.nombre;
    }
}
