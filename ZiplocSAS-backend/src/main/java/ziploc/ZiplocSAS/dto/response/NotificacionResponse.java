package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import ziploc.ZiplocSAS.model.Notificacion;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificacionResponse {
    private String id;
    private String usuarioId;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;

    public static NotificacionResponse from(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .usuarioId(n.getUsuarioId())
                .mensaje(n.getMensaje())
                .fecha(n.getFecha())
                .leida(n.isLeida())
                .build();
    }
}