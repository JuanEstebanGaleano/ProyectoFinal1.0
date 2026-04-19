package ziploc.ZiplocSAS.model;

import jakarta.persistence.*;
import lombok.*;
import ziploc.ZiplocSAS.structures.ListaEnlazada;
import java.util.UUID;

@Entity @Table(name = "billeteras")
@Getter @Setter @NoArgsConstructor  // ← Reemplaza @Data por @Getter @Setter
public class Billetera {

    @Id
    private String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoBilletera tipo;

    private double saldo = 0;
    private boolean activa = true;

    @Column(nullable = false)
    private String usuarioId;

    private int totalTransacciones = 0;

    @Transient
    private ListaEnlazada<Transaccion> historialTransacciones = new ListaEnlazada<>();

    public Billetera(String nombre, TipoBilletera tipo, double saldoInicial, String usuarioId) {
        this.nombre = nombre; this.tipo = tipo;
        this.saldo = saldoInicial; this.usuarioId = usuarioId;
    }

    public boolean recargar(double m) {
        if (m <= 0) return false;
        saldo += m; return true;
    }

    public boolean retirar(double m) {
        if (m <= 0 || m > saldo) return false;
        saldo -= m; return true;
    }

    public void agregarTransaccion(Transaccion t) {
        if (historialTransacciones == null) historialTransacciones = new ListaEnlazada<>();
        historialTransacciones.agregar(t);
        totalTransacciones++;
    }

    public ListaEnlazada<Transaccion> getHistorialTransacciones() {
        if (historialTransacciones == null) historialTransacciones = new ListaEnlazada<>();
        return historialTransacciones;
    }

    public void registrarTransaccion() { totalTransacciones++; }

    // ── toString() LIMPIO ────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
                "Billetera[%s] %-20s | %-15s | Saldo: $%10.2f | Activa: %-5s | Txs: %d",
                id, nombre, tipo, saldo, activa ? "SÍ" : "NO", totalTransacciones
        );
    }
}