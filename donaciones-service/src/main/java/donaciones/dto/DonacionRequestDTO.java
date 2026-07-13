package donaciones.dto;

import java.util.List;

public record DonacionRequestDTO(
        String documentoDonante,
        String descripcion,
        List<BienDTO> bienes
) {}