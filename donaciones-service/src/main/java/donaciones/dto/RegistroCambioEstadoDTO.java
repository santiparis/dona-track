package donaciones.dto;

public record RegistroCambioEstadoDTO(
    String estadoAnterior,
    String estadoNuevo,
    String fecha,
    String justificacion
) {}
