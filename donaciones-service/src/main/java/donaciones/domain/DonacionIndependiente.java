package donaciones.domain;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;

public class DonacionIndependiente {
    private final Bien bien;
    private EstadoDonacionIndependiente estado = EstadoDonacionIndependiente.EN_DEPOSITO;
    private final List<RegistroCambioEstado<EstadoDonacionIndependiente>> historialEstados = new ArrayList<>();

    public DonacionIndependiente(
        Bien bien
    ) {
        this.bien = bien;
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

    public boolean fueEntregadaDespuesDe(LocalDate fechaLimite) {
        return this.historialEstados.stream()
                .filter(registro -> registro.estadoNuevo() == EstadoDonacionIndependiente.ENTREGADA)
                .map(RegistroCambioEstado::fecha)
                .map(fechaDate -> fechaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .anyMatch(fechaEntrega -> !fechaEntrega.isBefore(fechaLimite));
    }
}
