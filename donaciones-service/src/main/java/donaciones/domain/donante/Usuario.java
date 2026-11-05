package donaciones.domain.donante;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "usuario_id") private Long id;
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Column(name = "contrasenia")
    @JsonIgnore
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    protected Usuario() { }

    public Usuario(String nombreUsuario, String password, Rol rol) {
        this.nombreUsuario = nombreUsuario;
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
