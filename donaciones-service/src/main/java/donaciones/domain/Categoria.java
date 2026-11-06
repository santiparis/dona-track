package donaciones.domain;

public enum Categoria {
    ALIMENTOS("Alimentos"),
    MUEBLES("Muebles"),
    ROPA("Ropa"),
    ELECTRODOMESTICOS("Electrodomésticos");

    private final String nombre;

    Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return this.nombre;
    }
}
