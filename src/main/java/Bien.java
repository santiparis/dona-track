import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class Bien {
    private final Subcategoria subcategoria;
    private final Integer cantidad;
    private final String unidad;
    private final EstadoBien estado;
    private final LocalDate vencimiento;
    private final String descripcion;
    private final String foto;

    public Bien(
            @NotNull Subcategoria subcategoria,
            Integer cantidad,
            String unidad,
            String descripcion,
            String foto,
            EstadoBien estado,
            LocalDate vencimiento
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
        this.descripcion = descripcion;
        this.foto = foto;
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

    public LocalDate getVencimiento() {
        return this.vencimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFoto() {
        return foto;
    }
}
