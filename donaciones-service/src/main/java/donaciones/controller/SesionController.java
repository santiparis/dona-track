package donaciones.controller;

import io.javalin.http.Context;
import donaciones.domain.donante.*;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.notificacion.*;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.UsuarioRepository;
import donaciones.web.SesionUsuario;
import io.javalin.http.HttpStatus;
import org.hibernate.exception.ConstraintViolationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.persistence.PersistenceException;

public class SesionController {
  private final UsuarioRepository usuarios;

  public SesionController(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  public Map<String, Object> login() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Iniciar sesión");
    model.put("seccion", "login");
    return model;
  }

  public Map<String, Object> login(Context ctx) {
    Map<String, Object> model = login();
    model.put("registroExitoso", "ok".equals(ctx.queryParam("registro")));
    return model;
  }

  public void ingresar(Context ctx) {
    String nombreUsuario = Objects.requireNonNullElse(ctx.formParam("usuario"), "").trim();
    String password = ctx.formParam("password");
    Usuario usuario = nombreUsuario.isEmpty() ? null : usuarios.buscarPorNombreUsuario(nombreUsuario).orElse(null);
    if (usuario == null || !usuario.validarPassword(password) || usuario.getRol() == null) {
      Map<String, Object> model = login();
      model.put("error", "Usuario o contraseña incorrectos.");
      model.put("nombreUsuario", nombreUsuario);
      ctx.status(401).render("sesion/login", model);
      return;
    }

    SesionUsuario.iniciar(ctx, usuario);
    ctx.redirect(SesionUsuario.destino(usuario.getRol()), HttpStatus.SEE_OTHER);
  }

  public Map<String, Object> registro(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Registro");
    model.put("seccion", "registro");
    // La pestaña elegida viaja en la URL: /usuarios/nuevo?tipo=HUMANA|JURIDICA|ENTIDAD
    String tipo = Objects.requireNonNullElse(ctx.queryParam("tipo"), "HUMANA");
    model.put("tipo", List.of("HUMANA", "JURIDICA", "ENTIDAD").contains(tipo) ? tipo : "HUMANA");
    model.put("medio", "EMAIL");
    return model;
  }

  public void registrar(Context ctx) {
    try {
      String tipo = campo(ctx, "tipo");
      if (!List.of("HUMANA", "JURIDICA", "ENTIDAD").contains(tipo)) {
        throw new IllegalArgumentException("Elegí un tipo de cuenta válido.");
      }
      String email = validarEmail(obligatorio(ctx, "email", "El correo electrónico"),
          "Ingresá un correo válido para la cuenta.");
      String password = ctx.formParam("password");
      if (password == null || password.isBlank() || password.length() < 8 || password.length() > 128) {
        throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres.");
      }
      if (!password.equals(ctx.formParam("password2"))) {
        throw new IllegalArgumentException("Las contraseñas no coinciden.");
      }
      if (!"on".equals(ctx.formParam("acepta"))) {
        throw new IllegalArgumentException("Tenés que aceptar los términos y la política de privacidad.");
      }
      if (usuarios.buscarPorNombreUsuario(email).isPresent()) {
        mostrarErrorRegistro(ctx, 409, "Ya existe una cuenta con ese correo electrónico.");
        return;
      }

      // El servidor decide el rol. El formulario público nunca puede crear administradoras.
      Usuario usuario = new Usuario(email, password, tipo.equals("ENTIDAD") ? Rol.ENTIDAD : Rol.DONANTE);
      if (tipo.equals("ENTIDAD")) {
        registrarEntidad(ctx, email, usuario);
      } else {
        registrarDonante(ctx, tipo, email, usuario);
      }
      // Fuerza las restricciones antes de responder; el after de Main confirma la transacción.
      usuarios.entityManager().flush();
      ctx.redirect("/login?registro=ok", HttpStatus.SEE_OTHER);
    } catch (IllegalArgumentException e) {
      mostrarErrorRegistro(ctx, 400, e.getMessage());
    } catch (PersistenceException e) {
      // La restricción también cubre dos registros simultáneos del mismo correo.
      Throwable causa = e;
      while (causa != null) {
        if (causa instanceof ConstraintViolationException restriccion
            && restriccion.getConstraintName() != null
            && restriccion.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_usuario_nombre")) {
          mostrarErrorRegistro(ctx, 409, "Ya existe una cuenta con ese correo electrónico.");
          return;
        }
        causa = causa.getCause();
      }
      throw e;
    }
  }

  private void registrarDonante(Context ctx, String tipo, String email, Usuario usuario) {
    List<Contacto> contactos = new ArrayList<>();
    Contacto correo = new ContactoPorEmail(email);
    contactos.add(correo);
    String telefono = campo(ctx, "telefono");
    String whatsapp = campo(ctx, "whatsapp");
    Contacto sms = telefono.isEmpty() ? null : new ContactoPorSMS(telefono);
    Contacto wa = whatsapp.isEmpty() ? null : new ContactoPorWhatsApp(whatsapp);
    if (sms != null) contactos.add(sms);
    if (wa != null) contactos.add(wa);
    Contacto preferido = switch (campo(ctx, "medio")) {
      case "EMAIL" -> correo;
      case "SMS" -> sms;
      case "WHATSAPP" -> wa;
      default -> throw new IllegalArgumentException("Elegí un medio de contacto válido.");
    };
    if (preferido == null) {
      throw new IllegalArgumentException("Completá el teléfono del medio de contacto elegido.");
    }
    String documento = obligatorio(ctx, "documento", "El documento o CUIT");
    Persona persona;
    if (tipo.equals("HUMANA")) {
      Integer edad = null;
      if (!campo(ctx, "edad").isEmpty()) {
        try {
          edad = Integer.valueOf(campo(ctx, "edad"));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("La edad debe ser un número entero.");
        }
        if (edad < 0 || edad > 150) throw new IllegalArgumentException("La edad debe estar entre 0 y 150.");
      }
      if (!"DNI".equals(campo(ctx, "tipoDoc"))) throw new IllegalArgumentException("El tipo de documento debe ser DNI.");
      Genero genero = opcion(Genero.class, campo(ctx, "genero"), "Elegí un género válido.");
      persona = new PersonaHumana(obligatorio(ctx, "nombre", "El nombre"),
          obligatorio(ctx, "apellido", "El apellido"), edad, TipoDoc.DNI, documento, genero,
          campo(ctx, "direccion"), contactos, preferido, usuario);
    } else {
      RazonSocial razon = opcion(RazonSocial.class, campo(ctx, "tipoJuridica"), "Elegí un tipo de persona jurídica válido.");
      persona = new PersonaJuridica(TipoDoc.CUIT, documento, obligatorio(ctx, "razonSocial", "La razón social"),
          razon, campo(ctx, "rubro"), new ArrayList<>(), contactos, preferido, usuario);
    }
    new RepositorioPersonas().agregar(persona);
  }

  private void registrarEntidad(Context ctx, String email, Usuario usuario) {
    List<String> correos = new ArrayList<>();
    String representantes = Objects.requireNonNullElse(ctx.formParam("correos"), "");
    if (representantes.length() > 5000) throw new IllegalArgumentException("La lista de correos es demasiado larga.");
    for (String correo : representantes.split("\\R")) {
      if (!correo.isBlank()) {
        correos.add(validarEmail(correo.trim(), "Revisá los correos de representantes. Ingresá uno válido por línea."));
      }
    }
    Contacto contacto = new ContactoPorEmail(email);
    EntidadBeneficiaria entidad = new EntidadBeneficiaria(obligatorio(ctx, "razonSocial", "La razón social"),
        obligatorio(ctx, "direccion", "La dirección"), campo(ctx, "telefono"), correos,
        List.of(contacto), contacto, usuario);
    new EntidadBeneficiariaRepository().guardar(entidad);
  }

  private void mostrarErrorRegistro(Context ctx, int estado, String error) {
    Map<String, Object> model = registro(ctx);
    // Se conservan los datos del formulario, nunca las contraseñas.
    for (String campo : List.of("nombre", "apellido", "documento", "edad", "genero", "direccion",
        "razonSocial", "tipoJuridica", "rubro", "email", "telefono", "whatsapp", "medio", "correos")) {
      model.put(campo, Objects.requireNonNullElse(ctx.formParam(campo), ""));
    }
    String tipo = ctx.formParam("tipo");
    if (List.of("HUMANA", "JURIDICA", "ENTIDAD").contains(Objects.requireNonNullElse(tipo, ""))) model.put("tipo", tipo);
    model.put("acepta", "on".equals(ctx.formParam("acepta")));
    model.put("error", error);
    ctx.status(estado).render("sesion/registro", model);
  }

  private String campo(Context ctx, String nombre) {
    String valor = Objects.requireNonNullElse(ctx.formParam(nombre), "").trim();
    if (valor.length() > 255) throw new IllegalArgumentException("Los campos deben tener hasta 255 caracteres.");
    return valor;
  }

  private String obligatorio(Context ctx, String nombre, String etiqueta) {
    String valor = campo(ctx, nombre);
    if (valor.isEmpty()) throw new IllegalArgumentException(etiqueta + " es obligatorio.");
    return valor;
  }

  private String validarEmail(String email, String mensajeError) {
    if (email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
      throw new IllegalArgumentException(mensajeError);
    }
    return email.toLowerCase(Locale.ROOT);
  }

  private <E extends Enum<E>> E opcion(Class<E> tipo, String valor, String mensaje) {
    try {
      return Enum.valueOf(tipo, valor);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(mensaje);
    }
  }
}
