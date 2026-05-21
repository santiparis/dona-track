import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NecesidadRecurrente extends Necesidad {
    private final Date fechaInicio;
    private final Date fechaFin;
    private final Periodo periodo;
    // o private final Integer periodo  //seria un multiplo de 7

    public NecesidadRecurrente(
            EntidadBeneficiaria entidad,
            String descripcion,
            List<Bien> bienes,
            Date fechaInicio,
            Date fechaFin,
            Periodo periodo
    ) {
        super(entidad, descripcion, bienes);
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

    public boolean estaVencida() {
        return this.fechaFin != null && this.fechaFin.before(new Date());
    }

    public Periodo getPeriodo() {
        return this.periodo;
    }

    public NecesidadRecurrente crearSiguientePeriodo() {
        return new NecesidadRecurrente(
                this.getEntidad(),
                this.getDescripcion(),
                this.getBienes(),
                sumarPeriodo(this.fechaInicio),
                sumarPeriodo(this.fechaFin),
                this.periodo
        );
    }

    private Date sumarPeriodo(Date fecha) {
        if (fecha == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        if (this.periodo == Periodo.SEMANAL) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (this.periodo == Periodo.MENSUAL) {
            calendar.add(Calendar.MONTH, 1);
        }
        return calendar.getTime();
    }

    @Override
    public void actualizar() {
        return;
    }

    @Override
    public Optional<Necesidad> resolver(){
        return Optional.of(this.crearSiguientePeriodo());
    }
}
