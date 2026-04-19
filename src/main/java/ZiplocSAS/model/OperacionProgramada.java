package ZiplocSAS.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OperacionProgramada implements Comparable<OperacionProgramada> {
    private final String id;
    private final String usuarioId, billeteraOrigenId, billeteraDestinoId;
    private final TipoTransaccion tipo;
    private final double monto;
    private final LocalDateTime fechaEjecucion;
    private boolean ejecutada;
    private final String descripcion;

    public OperacionProgramada(String usuarioId, String billeteraOrigenId,
                               String billeteraDestinoId, TipoTransaccion tipo,
                               double monto, LocalDateTime fechaEjecucion, String descripcion) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.usuarioId = usuarioId; this.billeteraOrigenId = billeteraOrigenId;
        this.billeteraDestinoId = billeteraDestinoId; this.tipo = tipo;
        this.monto = monto; this.fechaEjecucion = fechaEjecucion;
        this.ejecutada = false; this.descripcion = descripcion;
    }

    @Override public int compareTo(OperacionProgramada o) { return this.fechaEjecucion.compareTo(o.fechaEjecucion); }

    public String getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public String getBilleteraOrigenId() { return billeteraOrigenId; }
    public String getBilleteraDestinoId() { return billeteraDestinoId; }
    public TipoTransaccion getTipo() { return tipo; }
    public double getMonto() { return monto; }
    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public boolean isEjecutada() { return ejecutada; }
    public String getDescripcion() { return descripcion; }
    public void marcarEjecutada() { this.ejecutada = true; }

    @Override
    public String toString() {
        return String.format("Op.Prog[%s] %s | %s | $%.2f | %s | %s",
                id, descripcion, tipo, monto,
                fechaEjecucion.toString().substring(0,16), ejecutada ? "EJECUTADA" : "PENDIENTE");
    }
}