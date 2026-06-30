import java.util.Date;

public record RegistroCambioEstado<T>(
        T estadoAnterior,
        T estadoNuevo,
        Date fecha,
        String justificacion
) {

}
