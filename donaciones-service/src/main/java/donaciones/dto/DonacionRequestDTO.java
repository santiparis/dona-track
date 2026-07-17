package donaciones.dto;

import java.util.List;

public record DonacionRequestDTO(
        Long idDonante,
        String descripcion,
        List<BienDTO> bienes
) {}