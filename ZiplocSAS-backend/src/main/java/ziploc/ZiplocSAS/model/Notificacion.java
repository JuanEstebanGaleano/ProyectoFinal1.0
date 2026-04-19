package ziploc.ZiplocSAS.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "notificaciones")
@Data @NoArgsConstructor
public class Notificacion {
    @Id
    private String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false, length = 500)
    private String mensaje;

    private LocalDateTime fecha = LocalDateTime.now();
    private boolean leida = false;

    public Notificacion(String usuarioId, String mensaje) {
        this.usuarioId = usuarioId;
        this.mensaje = mensaje;
    }
}