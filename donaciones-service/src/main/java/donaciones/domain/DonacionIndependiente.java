package donaciones.domain;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;

public class DonacionIndependiente {
    private final Bien bien;
    private EstadoDonacionIndependiente estado = EstadoDonacionIndependiente.EN_DEPOSITO;
    private final List<RegistroCambioEstado<EstadoDonacionIndependiente>> historialEstados = new ArrayList<>();
    private LocalDate fecha;

    public DonacionIndependiente(
        Bien bien
    ) {
        this.bien = bien;
        this.fecha = LocalDate.now();
        this.historialEstados.add(new RegistroCambioEstado<>(null, this.estado, new java.util.Date(), null));
    }

    public Bien getBien() {
        return this.bien;
    }

    public EstadoDonacionIndependiente getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoDonacionIndependiente estado) {
        if (this.estado != estado) {
            this.historialEstados.add(new RegistroCambioEstado<>(this.estado, estado, new java.util.Date(), null));
        }
        this.estado = estado;
    }

    public List<RegistroCambioEstado<EstadoDonacionIndependiente>> getHistorialEstados() {
        return historialEstados;
    }

    public LocalDate getFecha() {
        return this.fecha;
    }
}
