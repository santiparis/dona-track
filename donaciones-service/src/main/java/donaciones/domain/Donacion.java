package donaciones.domain;

import donaciones.domain.donante.Persona;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Donacion {
    private final Long ID;
    private Persona donante;
    private EntidadBeneficiaria entidadBeneficiaria;
    private Bien bien;
    private EstadoDonacion estado = EstadoDonacion.EN_DEPOSITO;
    private final List<RegistroCambioEstado<EstadoDonacion>> historialEstados = new ArrayList<>();
    private LocalDate fecha;

    public Donacion(
        Persona donante,
        Bien bien,
        Long ID
    ) {
        this.ID = ID;
        this.donante = donante;
        this.bien = bien;
        this.fecha = LocalDate.now();
        this.historialEstados.add(new RegistroCambioEstado<>(null, this.estado, new java.util.Date(), null));
    }

    public void actualizarDatos(Persona donante, Bien bien) {
        if (donante != null) {
            this.donante = donante;
        }
        if (bien != null) {
            this.bien = bien;
        }
    }

    public Bien getBien() {
        return this.bien;
    }

    public EstadoDonacion getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoDonacion estado) {
        if (this.estado != estado) {
            this.historialEstados.add(new RegistroCambioEstado<>(this.estado, estado, new java.util.Date(), null));
        }
        this.estado = estado;
    }

    public List<RegistroCambioEstado<EstadoDonacion>> getHistorialEstados() {
        return historialEstados;
    }

    public LocalDate getFecha() {
        return this.fecha;
    }

    public Long getID() {
        return ID;
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
