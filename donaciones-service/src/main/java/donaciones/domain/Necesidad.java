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
    @Column(name = "fecha_inicio") private java.time.LocalDate fechaInicio;
    @Column(name = "fecha_fin") private java.time.LocalDate fechaFin;
    @Enumerated(EnumType.STRING) @Column(name = "periodo") private Periodo periodo;
    @Transient private PoliticaDeRenovacion renovacion;

    protected Necesidad() { }

    public Necesidad(
            String descripcion,
            PoliticaDeRenovacion renovacion,
            Map<Subcategoria, Integer> cantidadesRequeridas
    ) {
        this.descripcion = descripcion;
        this.renovacion = renovacion == null ? new SinRenovacion() : renovacion;
        this.fechaInicio = this.renovacion.getFechaInicio();
        this.fechaFin = this.renovacion.getFechaFin();
        this.periodo = this.renovacion.getPeriodo();
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
        if (renovacion == null) {
            renovacion = periodo == null
                    ? new SinRenovacion()
                    : new RenovacionPeriodica(fechaInicio, fechaFin, periodo);
        }
        return this.renovacion;
    }

    public java.time.LocalDate getFechaInicio() { return fechaInicio; }
    public java.time.LocalDate getFechaFin() { return fechaFin; }
    public Periodo getPeriodo() { return periodo; }

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
