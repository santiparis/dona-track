package donaciones.dto;

public record DonanteRequestDTO(
        String tipo,
        String documento,
        String nombre,

        String apellido,
        Integer edad,
        String direccion,

        String razonSocial,
        String rubro
) {}
