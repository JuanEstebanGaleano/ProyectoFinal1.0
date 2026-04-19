package ziploc.ZiplocSAS.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "billeteras")
@Data @NoArgsConstructor
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

    public Billetera(String nombre, TipoBilletera tipo, double saldoInicial, String usuarioId) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = saldoInicial;
        this.usuarioId = usuarioId;
    }

    public boolean recargar(double m) {
        if (m <= 0) return false;
        saldo += m; return true;
    }

    public boolean retirar(double m) {
        if (m <= 0 || m > saldo) return false;
        saldo -= m; return true;
    }

    public void registrarTransaccion() { totalTransacciones++; }
}