import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class Bien {
    private final Subcategoria subcategoria;
    private final Integer cantidad;
    private final String unidad;
    private final EstadoBien estado;
    private final Date vencimiento;

    public Bien(
            @NotNull Subcategoria subcategoria,
            Integer cantidad,
            String unidad,
            EstadoBien estado,
            Date vencimiento
    ) {
        if(subcategoria.requiereEstado() && estado == null) {
            throw new IllegalArgumentException("La subcategoria requiere estado");
        }
        if(subcategoria.requiereVencimiento() && vencimiento == null) {
            throw new IllegalArgumentException("La subcategoria requiere vencimiento");
        }
        this.subcategoria = subcategoria;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.estado = estado;
        this.vencimiento = vencimiento;
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

    public EstadoBien getEstado() {
        return this.estado;
    }

    public Date getVencimiento() {
        return this.vencimiento;
    }
}
