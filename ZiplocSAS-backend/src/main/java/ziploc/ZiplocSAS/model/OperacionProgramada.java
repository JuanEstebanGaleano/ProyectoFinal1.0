package ziploc.ZiplocSAS.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "operaciones_programadas")
@Data @NoArgsConstructor
public class OperacionProgramada implements Comparable<OperacionProgramada> {
    @Id
    private String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    private String usuarioId;
    private String billeteraOrigenId;
    private String billeteraDestinoId;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    private double monto;
    private LocalDateTime fechaEjecucion;
    private boolean ejecutada = false;
    private String descripcion;

    public OperacionProgramada(String usuarioId, String bO, String bD,
                               TipoTransaccion tipo, double monto,
                               LocalDateTime fecha, String desc) {
        this.usuarioId = usuarioId;
        this.billeteraOrigenId = bO;
        this.billeteraDestinoId = bD;
        this.tipo = tipo;
        this.monto = monto;
        this.fechaEjecucion = fecha;
        this.descripcion = desc;
    }

    public void marcarEjecutada() { this.ejecutada = true; }

    @Override
    public int compareTo(OperacionProgramada o) {
        return this.fechaEjecucion.compareTo(o.fechaEjecucion);
    }
}