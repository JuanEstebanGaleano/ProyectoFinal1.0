package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import ziploc.ZiplocSAS.model.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UsuarioResponse {
    private String id;
    private String nombre;
    private String email;
    private String cedula;
    private int puntosTotales;
    private NivelUsuario nivel;
    private int totalBilleteras;
    private long notificacionesSinLeer;

    public static UsuarioResponse from(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .cedula(u.getCedula())
                .puntosTotales(u.getPuntosTotales())
                .nivel(u.getNivel())
                .build();
    }
}