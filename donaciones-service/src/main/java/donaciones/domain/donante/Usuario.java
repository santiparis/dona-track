package donaciones.domain.donante;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.Locale;

@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_nombre", columnNames = "nombre_usuario"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "usuario_id") private Long id;
    @Column(name = "nombre_usuario", nullable = false)
    private String nombreUsuario;
    @Column(name = "contrasenia")
    @JsonIgnore
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    protected Usuario() { }

    public Usuario(String nombreUsuario, String password, Rol rol) {
        if (nombreUsuario == null || nombreUsuario.isBlank() || nombreUsuario.trim().length() > 255) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio y admite hasta 255 caracteres.");
        }
        this.nombreUsuario = nombreUsuario.trim().toLowerCase(Locale.ROOT);
        this.rol = Objects.requireNonNull(rol, "El rol del usuario es obligatorio.");
        this.passwordHash = HashContrasenia.generar(password);
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public boolean validarPassword(String password) {
        return HashContrasenia.verificar(password, passwordHash);
    }
    public Long getId() { return id; }

    public Rol getRol() {
        return rol;
    }
}
