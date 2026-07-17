package donaciones.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Necesidad {
    private Long id;
    private final String descripcion;
    private final Map<Subcategoria, Integer> cantidadesRequeridas;
    private Map<Subcategoria, Integer> cantidadesSuplidas;
    private final PoliticaDeRenovacion renovacion;

    public Necesidad(
            String descripcion,
            PoliticaDeRenovacion renovacion,
            Map<Subcategoria, Integer> cantidadesRequeridas
    ) {
        this.descripcion = descripcion;
        this.renovacion = renovacion;
        this.cantidadesRequeridas = new HashMap<>(cantidadesRequeridas);
        this.cantidadesSuplidas = new HashMap<>(cantidadesRequeridas);
        this.cantidadesSuplidas.replaceAll(((subcategoria, integer) -> 0));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    public boolean seSatisfaceCon(Bien bienDonado) {
        if (bienDonado == null || bienDonado.getSubcategoria() == null) {
            return false;
        }
        return this.cantidadesRequeridas.containsKey(bienDonado.getSubcategoria());
    }
}