package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import ziploc.ZiplocSAS.model.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BilleteraResponse {
    private String id;
    private String nombre;
    private TipoBilletera tipo;
    private double saldo;
    private boolean activa;
    private String usuarioId;
    private int totalTransacciones;

    public static BilleteraResponse from(Billetera b) {
        return BilleteraResponse.builder()
                .id(b.getId())
                .nombre(b.getNombre())
                .tipo(b.getTipo())
                .saldo(b.getSaldo())
                .activa(b.isActiva())
                .usuarioId(b.getUsuarioId())
                .totalTransacciones(b.getTotalTransacciones())
                .build();
    }
}