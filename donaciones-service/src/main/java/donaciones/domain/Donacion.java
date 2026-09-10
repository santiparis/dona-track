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

    // TODO: las notificaciones pasan al controller. La donacion solo cambia su estado.
    public void asignarA(EntidadBeneficiaria entidad) {
        this.entidadBeneficiaria = entidad;
        this.cambiarEstado(EstadoDonacion.ASIGNADA, null);
        // this.donante.notificar("Su donación ha sido asignada a la entidad: " + entidad.getRazonSocial());
        // entidad.notificar("Se le ha asignado satisfactoriamente una nueva donación.");
    }

    public void iniciarTraslado(String urlMapaSeguimiento) {
        this.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
        // String mensaje = "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + urlMapaSeguimiento;
        // this.donante.notificar(mensaje);
        // this.entidadBeneficiaria.notificar(mensaje);
    }

    public void confirmarEntrega(String camion) {
        this.cambiarEstado(EstadoDonacion.ENTREGADA, null);
        // String mensaje = "Entrega realizada. Fecha/Hora: " + new Date() + " | Camión: " + camion;
        // this.donante.notificar(mensaje);
        // this.entidadBeneficiaria.notificar(mensaje);
    }

    public void registrarEntregaFallida(String motivo) {
        this.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, motivo);
        // String mensaje = "Alerta: Entrega no satisfactoria. Motivo: " + motivo;
        // this.donante.notificar(mensaje);
        // this.entidadBeneficiaria.notificar(mensaje);
    }

    private void cambiarEstado(EstadoDonacion nuevo, String justificacion) {
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

    // TODO: se elimina cuando los services dejen de cambiar el estado por afuera del dominio.
    public void setEstado(EstadoDonacion estado) {
        this.cambiarEstado(estado, null);
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
