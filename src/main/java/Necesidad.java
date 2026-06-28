import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class Necesidad {
    private final EntidadBeneficiaria entidad;
    private final String descripcion;
    private final List<Bien> bienes;
    private final HashMap<Subcategoria, Integer> cantidadesSuplidas = new HashMap<>();

    public Necesidad(
            EntidadBeneficiaria entidad,
            String descripcion,
            List<Bien> bienes
    ) {
        this.entidad = entidad;
        this.descripcion = descripcion;
        this.bienes = bienes;
    }

    public Necesidad(
            EntidadBeneficiaria entidad,
            Subcategoria subcategoria,
            Integer cantidad,
            String descripcion
    ) {
        this(
                entidad,
                descripcion,
                List.of(new Bien(
                        subcategoria,
                        cantidad,
                        null,
                        subcategoria.requiereEstado() ? EstadoBien.USADO : null,
                        subcategoria.requiereVencimiento() ? new java.util.Date() : null
                ))
        );
    }

    public Subcategoria getSubcategoria() {return this.subcategoria; }

    public EntidadBeneficiaria getEntidad() {
        return this.entidad;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public List<Bien> getBienes() {
        return this.bienes;
    }

    public HashMap<Subcategoria, Integer> getCantidades() {
        HashMap<Subcategoria, Integer> cantidades = new HashMap<>();
        for(Bien bien : this.bienes) {
            cantidades.put(
                    bien.getSubcategoria(),
                    cantidades.getOrDefault(bien.getSubcategoria(), 0) + bien.getCantidad()
            );
        }
        return cantidades;
    }

    public HashMap<Subcategoria, Integer> getCantidadesPendientes() {
        HashMap<Subcategoria, Integer> cantidadesPendientes = new HashMap<>();
        HashMap<Subcategoria, Integer> cantidadesTotales = this.getCantidades();

        for (Subcategoria subcategoria : cantidadesTotales.keySet()) {
            Integer cantidadPendiente = cantidadesTotales.get(subcategoria)
                    - cantidadesSuplidas.getOrDefault(subcategoria, 0);
            if (cantidadPendiente > 0) {
                cantidadesPendientes.put(subcategoria, cantidadPendiente);
            }
        }

        return cantidadesPendientes;
    }

    public Integer getCantidadPendiente(Bien bien) {
        return this.getCantidadesPendientes().getOrDefault(bien.getSubcategoria(), 0);
    }

    public void registrarSuplido(Bien bien, Integer cantidad) {
        if (cantidad <= 0) {
            return;
        }
        Subcategoria subcategoria = bien.getSubcategoria();
        Integer cantidadActual = cantidadesSuplidas.getOrDefault(subcategoria, 0);
        Integer cantidadObjetivo = this.getCantidades().getOrDefault(subcategoria, 0);
        cantidadesSuplidas.put(subcategoria, Math.min(cantidadActual + cantidad, cantidadObjetivo));
    }

    public void restarSuplido(Bien bien, Integer cantidad) {
        if (cantidad <= 0) {
            return;
        }
        Subcategoria subcategoria = bien.getSubcategoria();
        Integer cantidadActual = cantidadesSuplidas.getOrDefault(subcategoria, 0);
        Integer nuevaCantidad = Math.max(cantidadActual - cantidad, 0);
        cantidadesSuplidas.put(subcategoria, nuevaCantidad);
    }

    public boolean estaSatisfecha() {
        return this.getCantidadesPendientes().isEmpty();
    }

    public void actualizar() {}

    public Optional<Necesidad> resolver() {
        return Optional.empty();
    }

}
