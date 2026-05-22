public enum Subcategoria {
    BANCOS(Categoria.MUEBLE, true, false,"Bancos"),
    SILLAS(Categoria.MUEBLE, true, false,"Sillas"),
    MESAS(Categoria.MUEBLE, true, false,"Mesas"),
    FIDEOS(Categoria.ALIMENTO, false, false,"Fideos"),
    ARROZ(Categoria.ALIMENTO, false, false,"Arroz"),
    LEGUMBRES(Categoria.ALIMENTO, false, false,"Legumbres"),
    ACEITE(Categoria.ALIMENTO, false, false,"Aceite"),
    LECHE(Categoria.ALIMENTO, false, true,"Leche"),
    CAMPERAS(Categoria.VESTIMENTA, true, false,"Camperas"),
    REMERAS(Categoria.VESTIMENTA, true, false,"Remeras"),
    PANTALONES(Categoria.VESTIMENTA, true, false,"Pantalones"),
    ROPA_INFANTIL(Categoria.VESTIMENTA, true, false,"Ropa infantil"),
    FRAZADAS(Categoria.ABRIGO, true, false,"Frazadas"),
    COLCHONES(Categoria.DESCANSO, true, false,"Colchones");

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
