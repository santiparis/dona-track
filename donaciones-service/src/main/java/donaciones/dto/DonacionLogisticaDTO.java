package donaciones.dto;

public record DonacionLogisticaDTO(
    Long donacionID,
    int cantidadBienes,
    String unidad,
    String destino,
    String entidadNombre
) {}