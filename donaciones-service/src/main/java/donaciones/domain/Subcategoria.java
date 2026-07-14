package donaciones.domain;

import java.util.Objects;

public class Subcategoria {
    public static final Subcategoria BANCOS = new Subcategoria(Categoria.MUEBLES, true, false, "Bancos");
    public static final Subcategoria SILLAS = new Subcategoria(Categoria.MUEBLES, true, false, "Sillas");
    public static final Subcategoria MESAS = new Subcategoria(Categoria.MUEBLES, true, false, "Mesas");
    public static final Subcategoria FIDEOS = new Subcategoria(Categoria.ALIMENTOS, false, false, "Fideos");
    public static final Subcategoria ARROZ = new Subcategoria(Categoria.ALIMENTOS, false, false, "Arroz");
    public static final Subcategoria LEGUMBRES = new Subcategoria(Categoria.ALIMENTOS, false, false, "Legumbres");
    public static final Subcategoria ACEITE = new Subcategoria(Categoria.ALIMENTOS, false, false, "Aceite");
    public static final Subcategoria LECHE = new Subcategoria(Categoria.ALIMENTOS, false, true, "Leche");
    public static final Subcategoria CAMPERAS = new Subcategoria(Categoria.ROPA, true, false, "Camperas");
    public static final Subcategoria REMERAS = new Subcategoria(Categoria.ROPA, true, false, "Remeras");
    public static final Subcategoria PANTALONES = new Subcategoria(Categoria.ROPA, true, false, "Pantalones");
    public static final Subcategoria ROPA_INFANTIL = new Subcategoria(Categoria.ROPA, true, false, "Ropa infantil");
    public static final Subcategoria FRAZADAS = new Subcategoria(Categoria.MUEBLES, true, false, "Frazadas");
    public static final Subcategoria COLCHONES = new Subcategoria(Categoria.MUEBLES, true, false, "Colchones");

    private final Categoria categoria;
    private final boolean requiereEstado;
    private final boolean requiereVencimiento;
    private final String nombre;

    public Subcategoria(
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

    public String nombre() {
        return this.nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subcategoria that)) return false;
        return requiereEstado == that.requiereEstado
                && requiereVencimiento == that.requiereVencimiento
                && categoria == that.categoria
                && Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoria, requiereEstado, requiereVencimiento, nombre);
    }
}
