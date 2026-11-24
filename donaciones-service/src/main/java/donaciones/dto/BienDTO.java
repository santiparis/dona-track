package donaciones.dto;

public record BienDTO(
        boolean requiereEstado,
        boolean requiereVencimiento,
        String nombreSubcategoria,
        Integer cantidad,
        String descripcion,
        String estado,
        String vencimiento,
        String foto
) {}