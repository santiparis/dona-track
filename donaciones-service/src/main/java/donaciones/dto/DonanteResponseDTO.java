package donaciones.dto;

import java.util.List;

public record DonanteResponseDTO(
    String tipo,
    String documento,
    String nombre,
    String apellido,
    Integer edad,
    String direccion,
    String razonSocial,
    String rubro,
    List<ContactoResponseDTO> contactos
) {}
