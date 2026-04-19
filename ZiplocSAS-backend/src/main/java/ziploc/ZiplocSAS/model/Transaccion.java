package ziploc.ZiplocSAS.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "transacciones")
@Data @NoArgsConstructor
public class Transaccion implements Comparable<Transaccion> {
    @Id
    private String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    private double valor;
    private String billeteraOrigenId;
    private String billeteraDestinoId;
    private String usuarioId;

    @Enumerated(EnumType.STRING)
    private EstadoTransaccion estado = EstadoTransaccion.COMPLETADA;

    private int puntosGenerados;

    @Enumerated(EnumType.STRING)
    private NivelRiesgo nivelRiesgo = NivelRiesgo.BAJO;

    private boolean sospechosa = false;

    public Transaccion(TipoTransaccion tipo, double valor,
                       String bOrigen, String bDestino, String usuarioId) {
        this.tipo = tipo;
        this.valor = valor;
        this.billeteraOrigenId = bOrigen;
        this.billeteraDestinoId = bDestino;
        this.usuarioId = usuarioId;
        this.puntosGenerados = calcularPuntos();
    }

    private int calcularPuntos() {
        return switch (tipo) {
            case RECARGA                -> (int)(valor / 100);
            case RETIRO                 -> (int)(valor / 100) * 2;
            case TRANSFERENCIA_ENVIADA,
                 TRANSFERENCIA_RECIBIDA -> (int)(valor / 100) * 3;
            case PAGO_PROGRAMADO        -> (int)(valor / 100) * 2 + 10;
            default                     -> 0;
        };
    }

    public void revertir() {
        this.estado = EstadoTransaccion.REVERTIDA;
        this.puntosGenerados = 0;
    }

    @Override
    public int compareTo(Transaccion o) { return this.fecha.compareTo(o.fecha); }
}