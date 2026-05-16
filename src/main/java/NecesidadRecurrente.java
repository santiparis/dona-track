import java.util.Date;

public class NecesidadRecurrente extends Necesidad {
    private final Date fechaInicio;
    private final Date fechaFin;
    private final Periodo periodo;

    public NecesidadRecurrente(
            EntidadBeneficiaria entidad,
            Subcategoria subcategoria,
            Integer cantidad,
            String descripcion,
            Date fechaInicio,
            Date fechaFin,
            Periodo periodo
    ) {
        super(entidad, subcategoria, cantidad, descripcion);
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.periodo = periodo;
    }


    public Date getFechaInicio() {
        return this.fechaInicio;
    }

    public Date getFechaFin() {
        return this.fechaFin;
    }

    @Override
    public void actualizar() {
        return;
    }

    @Override
    public void resolver(){
        return;
    }
}
