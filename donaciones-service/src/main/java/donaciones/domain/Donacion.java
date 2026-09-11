package donaciones.domain;

import donaciones.domain.donante.Persona;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;

public class Donacion {
    private Long id;
    private Persona donante;
    private EntidadBeneficiaria entidadBeneficiaria;
    private Bien bien;
    private EstadoDonacion estado = EstadoDonacion.EN_DEPOSITO;
    private final List<RegistroCambioEstado<EstadoDonacion>> historialEstados = new ArrayList<>();
    private final LocalDate fecha;

    public Donacion(
        Persona donante,
        Bien bien
    ) {
        this.donante = donante;
        this.bien = bien;
        this.fecha = LocalDate.now();
        this.historialEstados.add(new RegistroCambioEstado<>(null, this.estado, new Date(), null));
    }

    public void actualizarDatos(Persona donante, Bien bien) {
        if (donante != null) {
            this.donante = donante;
        }
        if (bien != null) {
            this.bien = bien;
        }
    }

    public void asignarA(EntidadBeneficiaria entidad) {
        this.entidadBeneficiaria = entidad;
        this.cambiarEstado(EstadoDonacion.ASIGNADA, null);
    }

    public void cambiarEstado(EstadoDonacion nuevo, String justificacion) {
        if (this.estado != nuevo) {
            this.historialEstados.add(new RegistroCambioEstado<>(this.estado, nuevo, new Date(), justificacion));
        }
        this.estado = nuevo;
    }

    public Bien getBien() {
        return this.bien;
    }

    public EstadoDonacion getEstado() {
        return this.estado;
    }

    public List<RegistroCambioEstado<EstadoDonacion>> getHistorialEstados() {
        return historialEstados;
    }

    public LocalDate getFecha() {
        return this.fecha;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Persona getDonante() {
        return this.donante;
    }

    public EntidadBeneficiaria getEntidadBeneficiaria() {
        return this.entidadBeneficiaria;
    }

    public void setEntidadBeneficiaria(EntidadBeneficiaria entidadBeneficiaria) {
        this.entidadBeneficiaria = entidadBeneficiaria;
    }
}
