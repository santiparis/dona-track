package donaciones.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.*;

@Entity
@Table(name = "necesidad")
public class Necesidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "necesidad_id") private Long id;
    @Column(name = "descripcion") private String descripcion;
    @ElementCollection @CollectionTable(name = "cantidad_requerida", joinColumns = @JoinColumn(name = "necesidad_id"))
    @MapKeyEnumerated(EnumType.STRING) @MapKeyColumn(name = "subcategoria_id") @Column(name = "cantidad_requerida")
    private Map<Subcategoria, Integer> cantidadesRequeridas = new HashMap<>();
    @ElementCollection @CollectionTable(name = "cantidad_suplida", joinColumns = @JoinColumn(name = "necesidad_id"))
    @MapKeyEnumerated(EnumType.STRING) @MapKeyColumn(name = "subcategoria_id") @Column(name = "cantidad_suplida")
    private Map<Subcategoria, Integer> cantidadesSuplidas = new HashMap<>();
    @Transient private PoliticaDeRenovacion renovacion = new SinRenovacion();

    protected Necesidad() { }

    public Necesidad(
            String descripcion,
            PoliticaDeRenovacion renovacion,
            Map<Subcategoria, Integer> cantidadesRequeridas
    ) {
        this.descripcion = descripcion;
        this.renovacion = renovacion == null ? new SinRenovacion() : renovacion;
        this.cantidadesRequeridas = new HashMap<>(cantidadesRequeridas);
        this.cantidadesSuplidas = new HashMap<>(cantidadesRequeridas);
        this.cantidadesSuplidas.replaceAll(((subcategoria, integer) -> 0));
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Map<Subcategoria, Integer> getCantidadesRequeridas() {
        return Map.copyOf(cantidadesRequeridas);
    }

    public Map<Subcategoria, Integer> getCantidadesSuplidas() {
        return Map.copyOf(cantidadesSuplidas);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PoliticaDeRenovacion getRenovacion() {
        return renovacion;
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "fecha_inicio")
    public java.time.LocalDate getFechaInicio() {
        return renovacion.getFechaInicio();
    }

    public void setFechaInicio(java.time.LocalDate fechaInicio) {
        actualizarRenovacion(
                fechaInicio,
                renovacion.getFechaFin(),
                renovacion.getPeriodo()
        );
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "fecha_fin")
    public java.time.LocalDate getFechaFin() {
        return renovacion.getFechaFin();
    }

    public void setFechaFin(java.time.LocalDate fechaFin) {
        actualizarRenovacion(
                renovacion.getFechaInicio(),
                fechaFin,
                renovacion.getPeriodo()
        );
    }

    @Access(AccessType.PROPERTY)
    @Enumerated(EnumType.STRING)
    @Column(name = "periodo")
    public Periodo getPeriodo() {
        return renovacion.getPeriodo();
    }

    public void setPeriodo(Periodo periodo) {
        actualizarRenovacion(
                renovacion.getFechaInicio(),
                renovacion.getFechaFin(),
                periodo
        );
    }

    private void actualizarRenovacion(
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            Periodo periodo
    ) {
        renovacion = fechaInicio == null && fechaFin == null && periodo == null
                ? new SinRenovacion()
                : new RenovacionPeriodica(fechaInicio, fechaFin, periodo);
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
