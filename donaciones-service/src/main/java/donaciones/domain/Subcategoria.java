package donaciones.domain;

public enum Subcategoria {
    BANCOS(Categoria.MUEBLES, true, false,"Bancos"),
    SILLAS(Categoria.MUEBLES, true, false,"Sillas"),
    MESAS(Categoria.MUEBLES, true, false,"Mesas"),
    FIDEOS(Categoria.ALIMENTOS, false, false,"Fideos"),
    ARROZ(Categoria.ALIMENTOS, false, false,"Arroz"),
    LEGUMBRES(Categoria.ALIMENTOS, false, false,"Legumbres"),
    ACEITE(Categoria.ALIMENTOS, false, false,"Aceite"),
    LECHE(Categoria.ALIMENTOS, false, true,"Leche"),
    CAMPERAS(Categoria.ROPA, true, false,"Camperas"),
    REMERAS(Categoria.ROPA, true, false,"Remeras"),
    PANTALONES(Categoria.ROPA, true, false,"Pantalones"),
    ROPA_INFANTIL(Categoria.ROPA, true, false,"Ropa infantil"),
    FRAZADAS(Categoria.ROPA, true, false,"Frazadas");

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
