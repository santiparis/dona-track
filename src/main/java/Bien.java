import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

public class Bien {
    private final Subcategoria subcategoria;
    private final Integer cantidad;
    private final String unidad;
    private final EstadoBien estado;
    private final Date vencimiento;
    private final String descripcion;
    private final String foto;

    public Bien(
            @NotNull Subcategoria subcategoria,
            Integer cantidad,
            String unidad,
            EstadoBien estado,
            Date vencimiento
    ) {
        this(subcategoria, cantidad, unidad, estado, vencimiento, null, null);
    }

    public Bien(
            @NotNull Subcategoria subcategoria,
            Integer cantidad,
            String unidad,
            EstadoBien estado,
            Date vencimiento,
            String descripcion,
            String foto
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

    public Date getVencimiento() {
        return this.vencimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public boolean comparteSegmentoCon(Bien otroBien) {
        return this.subcategoria == otroBien.subcategoria
                && Objects.equals(this.unidad, otroBien.unidad)
                && this.estado == otroBien.estado
                && Objects.equals(this.vencimiento, otroBien.vencimiento);
    }

    public Bien conCantidad(Integer nuevaCantidad) {
        return new Bien(
                this.subcategoria,
                nuevaCantidad,
                this.unidad,
                this.estado,
                this.vencimiento,
                this.descripcion,
                this.foto
        );
    }
}
