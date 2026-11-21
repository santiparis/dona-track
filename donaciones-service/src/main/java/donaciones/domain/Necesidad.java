package donaciones.domain;

import java.time.LocalDate;
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
    @Column(name = "fecha_inicio") private LocalDate fechaInicio;
    @Column(name = "fecha_fin") private LocalDate fechaFin;
    @Enumerated(EnumType.STRING) @Column(name = "periodo") private Periodo periodo;

    protected Necesidad() { }

    public Necesidad(
            String descripcion,
            boolean renovacion,
            LocalDate fechaInicio,
            Periodo periodo,
            Map<Subcategoria, Integer> cantidadesRequeridas
    ) {
        asignarDatos(descripcion, renovacion, fechaInicio, periodo, cantidadesRequeridas);
        this.cantidadesSuplidas = new HashMap<>(cantidadesRequeridas);
        this.cantidadesSuplidas.replaceAll(((subcategoria, integer) -> 0));
    }

    // Edita la necesidad con los datos de otra que no se guarda; lo ya suplido se conserva
    public void actualizarCon(Necesidad datos) {
        if (!datos.cantidadesRequeridas.keySet().equals(this.cantidadesRequeridas.keySet())) {
            throw new IllegalArgumentException("La subcategoría no se puede cambiar: eliminá la necesidad y registrá otra");
        }
        if (datos.isRenovacion() != this.isRenovacion()) {
            throw new IllegalArgumentException("El tipo no se puede cambiar: eliminá la necesidad y registrá otra");
        }
        datos.cantidadesRequeridas.forEach((subcategoria, cantidad) -> {
            if (cantidad < this.cantidadesSuplidas.getOrDefault(subcategoria, 0)) {
                throw new IllegalArgumentException("La cantidad no puede ser menor a lo que ya se recibió");
            }
        });
        asignarDatos(datos.descripcion, datos.isRenovacion(), datos.fechaInicio, datos.periodo, datos.cantidadesRequeridas);
    }

    private void asignarDatos(
            String descripcion,
            boolean renovacion,
            LocalDate fechaInicio,
            Periodo periodo,
            Map<Subcategoria, Integer> cantidadesRequeridas
    ) {
        this.descripcion = descripcion;
        if (renovacion && (fechaInicio == null || periodo == null)) {
            throw new IllegalArgumentException("Una necesidad renovable requiere fecha de inicio y período");
        }
        this.periodo = renovacion ? periodo : null;
        // La fecha de inicio se guarda en los dos tipos; solo la recurrente la usa para calcular la fecha de fin
        this.fechaInicio = fechaInicio;
        this.fechaFin = calcularFechaFin();
        this.cantidadesRequeridas.clear();
        this.cantidadesRequeridas.putAll(cantidadesRequeridas);
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

    // Sin uso desde que actualizarNecesidadPorId edita la necesidad existente (para el clean up)
    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRenovacion() {
        return periodo != null;
    }

    public boolean seRenueva() {
        return isRenovacion();
    }

    public boolean estaVencida() {
        return isRenovacion() && fechaFin.isBefore(LocalDate.now());
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    private LocalDate calcularFechaFin() {
        if (periodo == null || fechaInicio == null) {
            return null;
        }
        return periodo == Periodo.SEMANAL
                ? fechaInicio.plusDays(7)
                : fechaInicio.plusMonths(1);
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
