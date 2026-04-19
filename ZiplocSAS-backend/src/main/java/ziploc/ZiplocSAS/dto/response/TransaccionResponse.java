package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import ziploc.ZiplocSAS.model.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TransaccionResponse {
    private String id;
    private LocalDateTime fecha;
    private TipoTransaccion tipo;
    private double valor;
    private String billeteraOrigenId;
    private String billeteraDestinoId;
    private EstadoTransaccion estado;
    private int puntosGenerados;
    private NivelRiesgo nivelRiesgo;
    private boolean sospechosa;

    public static TransaccionResponse from(Transaccion t) {
        return TransaccionResponse.builder()
                .id(t.getId())
                .fecha(t.getFecha())
                .tipo(t.getTipo())
                .valor(t.getValor())
                .billeteraOrigenId(t.getBilleteraOrigenId())
                .billeteraDestinoId(t.getBilleteraDestinoId())
                .estado(t.getEstado())
                .puntosGenerados(t.getPuntosGenerados())
                .nivelRiesgo(t.getNivelRiesgo())
                .sospechosa(t.isSospechosa())
                .build();
    }
}