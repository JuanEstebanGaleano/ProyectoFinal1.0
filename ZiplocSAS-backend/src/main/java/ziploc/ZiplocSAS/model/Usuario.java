package ziploc.ZiplocSAS.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "usuarios")
@Data @NoArgsConstructor
public class Usuario {
    @Id
    private String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String cedula;

    private int puntosTotales = 0;

    @Enumerated(EnumType.STRING)
    private NivelUsuario nivel = NivelUsuario.BRONCE;

    public Usuario(String nombre, String email, String cedula) {
        this.nombre = nombre;
        this.email = email;
        this.cedula = cedula;
    }

    public void sumarPuntos(int pts) {
        this.puntosTotales += pts;
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
    }

    public void descontarPuntos(int pts) {
        this.puntosTotales = Math.max(0, this.puntosTotales - pts);
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
    }
}
