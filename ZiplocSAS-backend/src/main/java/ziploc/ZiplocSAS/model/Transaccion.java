package ziploc.ZiplocSAS.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaccion implements Comparable<Transaccion> {
    private final String id;
    private final LocalDateTime fecha;
    private final TipoTransaccion tipo;
    private final double valor;
    private final String billeteraOrigenId;
    private final String billeteraDestinoId;
    private EstadoTransaccion estado;
    private int puntosGenerados;
    private NivelRiesgo nivelRiesgo;
    private boolean sospechosa;

    public Transaccion(TipoTransaccion tipo, double valor,
                       String billeteraOrigenId, String billeteraDestinoId) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.valor = valor;
        this.billeteraOrigenId = billeteraOrigenId;
        this.billeteraDestinoId = billeteraDestinoId;
        this.estado = EstadoTransaccion.COMPLETADA;
        this.nivelRiesgo = NivelRiesgo.BAJO;
        this.sospechosa = false;
        this.puntosGenerados = calcularPuntos();
    }

    private int calcularPuntos() {
        return switch (tipo) {
            case RECARGA -> (int)(valor / 100);
            case RETIRO -> (int)(valor / 100) * 2;
            case TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECIBIDA -> (int)(valor / 100) * 3;
            case PAGO_PROGRAMADO -> (int)(valor / 100) * 2 + 10;
            default -> 0;
        };
    }

    public void revertir() { this.estado = EstadoTransaccion.REVERTIDA; this.puntosGenerados = 0; }

    @Override public int compareTo(Transaccion o) { return this.fecha.compareTo(o.fecha); }

    public String getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public TipoTransaccion getTipo() { return tipo; }
    public double getValor() { return valor; }
    public String getBilleteraOrigenId() { return billeteraOrigenId; }
    public String getBilleteraDestinoId() { return billeteraDestinoId; }
    public EstadoTransaccion getEstado() { return estado; }
    public int getPuntosGenerados() { return puntosGenerados; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public boolean isSospechosa() { return sospechosa; }
    public void setEstado(EstadoTransaccion e) { this.estado = e; }
    public void setNivelRiesgo(NivelRiesgo r) { this.nivelRiesgo = r; }
    public void setSospechosa(boolean s) { this.sospechosa = s; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%.2f | %s -> %s | %s | Pts:%d%s",
                id, fecha.toString().substring(0,16), tipo, valor,
                billeteraOrigenId != null ? billeteraOrigenId : "EXT",
                billeteraDestinoId != null ? billeteraDestinoId : "EXT",
                estado, puntosGenerados,
                sospechosa ? " ⚠️[" + nivelRiesgo + "]" : "");
    }
}