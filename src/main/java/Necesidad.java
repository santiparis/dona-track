import java.util.*;

public class Necesidad {
    private final String descripcion;
    private final Map<Subcategoria, Integer> cantidadesRequeridas;
    private Map<Subcategoria, Integer> cantidadesSuplidas = new HashMap<>();
    private final PoliticaDeRenovacion renovacion;
    private final List<Bien> bienes = new ArrayList<>();

    public Necesidad(
            String descripcion,
            PoliticaDeRenovacion renovacion,
            Map<Subcategoria, Integer> cantidadesRequeridas
    ) {
        this.descripcion = descripcion;
        this.renovacion = renovacion;
        this.cantidadesRequeridas = cantidadesRequeridas;
        this.cantidadesSuplidas = cantidadesRequeridas;
        this.cantidadesSuplidas.replaceAll(((subcategoria, integer) -> integer = 0));
    }

    public PoliticaDeRenovacion getRenovacion() {
        return this.renovacion;
    }

    public void registrarSuplido(Bien bien) {
        if (bien.getCantidad() <= 0) {
            return;
        }
        Subcategoria subcategoria = bien.getSubcategoria();
        Integer cantidadActual = cantidadesSuplidas.getOrDefault(subcategoria, 0);
        cantidadesSuplidas.put(subcategoria, Math.min(cantidadActual + bien.getCantidad(), this.cantidadesRequeridas.get(subcategoria)));
    }

    public boolean estaSatisfecha() {
        return this.cantidadesSuplidas.keySet().stream().allMatch(subcategoria -> Objects.equals(this.cantidadesSuplidas.get(subcategoria), this.cantidadesRequeridas.get(subcategoria)));
    }
    public boolean requiereSubcategoria(Subcategoria subcategoria) {
        return this.estaSatisfecha() && this.bienes.stream().anyMatch(bien -> bien.getSubcategoria().equals(subcategoria));
    }
}