package donaciones.domain;

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

    public Bien conCantidad(Integer nuevaCantidad){

        return new Bien(this.subcategoria, nuevaCantidad, this.unidad, this.descripcion, this.foto,this.estado,this.vencimiento);
    }

    public boolean comparteSegmentoCon(Bien bien){
        if(this.subcategoria.requiereEstado() && bien.subcategoria.requiereEstado())
            return this.subcategoria.equals(bien.subcategoria) && this.estado == bien.estado;
        if(this.subcategoria.requiereVencimiento() && bien.subcategoria.requiereVencimiento())
            return this.subcategoria.equals(bien.subcategoria) && this.vencimiento.isEqual(bien.vencimiento);
        else
            return this.subcategoria.equals(bien.subcategoria);
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
