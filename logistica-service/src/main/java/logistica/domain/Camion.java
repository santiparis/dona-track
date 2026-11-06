package logistica.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "camiones")
public class Camion {
  // el id lo genera la base (columna autoincremental), no mas secuencias en el repositorio
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // la patente identifica al camion en el mundo real: no puede repetirse ni faltar
  @Column(unique = true, nullable = false)
  private String patente;

  @Column(name = "ultima_actualizacion", nullable = true)
  private LocalDateTime ultimaActualizacion = null;

  private double volumen;
  private double altura;
  private double cargaMax;

  // @Embedded: latitud y longitud son dos columnas mas de la tabla camiones.
  // El override las marca nullables: como en Coordenadas son double primitivos,
  // Hibernate las haria NOT NULL y no dejaria guardar un camion sin posicion.
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "latitud", column = @Column(name = "latitud", nullable = true)),
      @AttributeOverride(name = "longitud", column = @Column(name = "longitud", nullable = true))
  })
  private Coordenadas localizacion = null;

  private double velocidad = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false)
  private EstadoCamion estado;

  // los jsonProperty los usara jackson para crear los objetos de json a dominio
  @JsonCreator
  public Camion(@JsonProperty("patente") String patente,
                @JsonProperty("volumen") double volumen,
                @JsonProperty("altura") double altura,
                @JsonProperty("cargaMax") double cargaMax) {
    this.patente = patente;
    this.volumen = volumen;
    this.altura = altura;
    this.cargaMax = cargaMax;
    this.estado = EstadoCamion.DISPONIBLE;
  }

  // constructor sin argumentos que exige JPA para instanciar al leer de la base
  protected Camion() {
  }

  public Long getId() {
    return id;
  }

  // valida antes de mutar: si la velocidad es invalida el camion queda intacto
  public void actualizarLocalizacion(Coordenadas nuevaLocalizacion, double nuevaVelocidad) {
    if (nuevaVelocidad < 0) {
      throw new IllegalArgumentException("La velocidad no puede ser negativa");
    }
    this.localizacion = nuevaLocalizacion;
    this.velocidad = nuevaVelocidad;
    this.ultimaActualizacion = LocalDateTime.now(); // Marca de tiempo actual al reportar
  }

  public void actualizarDatos(String nuevaPatente,
                              double nuevoVolumen,
                              double nuevaAltura,
                              double nuevaCargaMax) {
    this.patente = nuevaPatente;
    this.volumen = nuevoVolumen;
    this.altura = nuevaAltura;
    this.cargaMax = nuevaCargaMax;
  }

  public boolean estaDisponible(){
    return this.estado == EstadoCamion.DISPONIBLE;
  }

  public Coordenadas getLocalizacion() {
    return localizacion;
  }

  public double getVelocidad() {
    return velocidad;
  }

  public double getCargaMax() {
    return cargaMax;
  }

  public double getAltura() {
    return altura;
  }

  public double getVolumen() {
    return volumen;
  }

  public String getPatente(){
    return this.patente;
  }

  // Solo lo usa gRPC (ubicación de camiones); sin el módulo jsr310, Jackson no puede serializar LocalDateTime
  @JsonIgnore
  public LocalDateTime getUltimaActualizacion() {
    return ultimaActualizacion;
  }

  public void setEstado(EstadoCamion nuevoEstado) { this.estado = nuevoEstado; }

  public void ocupar() {
    this.estado = EstadoCamion.EN_RUTA;
  }

  public void liberar() {
    this.estado = EstadoCamion.DISPONIBLE;
  }

  public void inhabilitar() {
    this.estado = EstadoCamion.NO_DISPONIBLE;
  }
}