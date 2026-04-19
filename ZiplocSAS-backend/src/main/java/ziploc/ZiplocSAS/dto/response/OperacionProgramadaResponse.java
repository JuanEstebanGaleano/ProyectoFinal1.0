package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import ziploc.ZiplocSAS.model.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OperacionProgramadaResponse {
    private String id;
    private String usuarioId;
    private String billeteraOrigenId;
    private String billeteraDestinoId;
    private TipoTransaccion tipo;
    private double monto;
    private LocalDateTime fechaEjecucion;
    private boolean ejecutada;
    private String descripcion;

    public static OperacionProgramadaResponse from(OperacionProgramada op) {
        return OperacionProgramadaResponse.builder()
                .id(op.getId())
                .usuarioId(op.getUsuarioId())
                .billeteraOrigenId(op.getBilleteraOrigenId())
                .billeteraDestinoId(op.getBilleteraDestinoId())
                .tipo(op.getTipo())
                .monto(op.getMonto())
                .fechaEjecucion(op.getFechaEjecucion())
                .ejecutada(op.isEjecutada())
                .descripcion(op.getDescripcion())
                .build();
    }
}