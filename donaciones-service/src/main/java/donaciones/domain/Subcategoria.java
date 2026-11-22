package donaciones.domain;

public enum Subcategoria {
    BANCOS(Categoria.MUEBLES, true, false,"Bancos", "unidades"),
    SILLAS(Categoria.MUEBLES, true, false,"Sillas", "unidades"),
    MESAS(Categoria.MUEBLES, true, false,"Mesas", "unidades"),
    FIDEOS(Categoria.ALIMENTOS, false, false,"Fideos", "kg"),
    ARROZ(Categoria.ALIMENTOS, false, false,"Arroz", "kg"),
    LEGUMBRES(Categoria.ALIMENTOS, false, false,"Legumbres", "kg"),
    ACEITE(Categoria.ALIMENTOS, false, false,"Aceite", "litros"),
    LECHE(Categoria.ALIMENTOS, false, true,"Leche", "litros"),
    CAMPERAS(Categoria.ROPA, true, false,"Camperas", "unidades"),
    REMERAS(Categoria.ROPA, true, false,"Remeras", "unidades"),
    PANTALONES(Categoria.ROPA, true, false,"Pantalones", "unidades"),
    ROPA_INFANTIL(Categoria.ROPA, true, false,"Ropa infantil", "unidades"),
    FRAZADAS(Categoria.ROPA, true, false,"Frazadas", "unidades"),
    VENTILADORES(Categoria.ELECTRODOMESTICOS, true, false,"Ventiladores", "unidades"),
    TELEVISORES(Categoria.ELECTRODOMESTICOS, true, false,"Televisores", "unidades"),
    ESTUFAS(Categoria.ELECTRODOMESTICOS, true, false,"Estufas", "unidades"),
    AGUA(Categoria.ALIMENTOS, false, false,"Agua", "litros"),
    AIRES_ACONDICIONADOS(Categoria.ELECTRODOMESTICOS, true, false,"Aires acondicionados", "unidades");

    private final Categoria categoria;
    private final boolean requiereEstado;
    private final boolean requiereVencimiento;
    private final String nombre;
    private final String unidad;

    Subcategoria(
            Categoria categoria,
            boolean requiereEstado,
            boolean requiereVencimiento,
            String nombre,
            String unidad
    ) {
        this.categoria = categoria;
        this.requiereEstado = requiereEstado;
        this.requiereVencimiento = requiereVencimiento;
        this.nombre = nombre;
        this.unidad = unidad;
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

    public String unidad() {
        return this.unidad;
    }
}
