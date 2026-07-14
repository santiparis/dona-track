package donaciones.dto;

import donaciones.domain.Subcategoria;

public record BienDTO(
        String categoria,
        boolean requiereEstado,
        boolean requiereVencimiento,
        String nombreSubcategoria,
        Integer cantidad,
        String unidad,
        String descripcion,
        String estado,
        String vencimiento,
        String foto
) {}