package donaciones.domain.donante;

import javax.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "usuario_id") private Long id;
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Column(name = "contrasenia")
    private String password;

    protected Usuario() { }

    public Usuario(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        // TODO: Almacenar la password de forma segura.
        this.password = password;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPassword() {
        return password;
    }
    public Long getId() { return id; }
}
