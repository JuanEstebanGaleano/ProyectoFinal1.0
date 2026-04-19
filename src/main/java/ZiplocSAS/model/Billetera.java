package ZiplocSAS.model;

import ZiplocSAS.structures.ListaEnlazada;
import java.util.UUID;

public class Billetera {
    private final String id;
    private String nombre;
    private TipoBilletera tipo;
    private double saldo;
    private boolean activa;
    private final String usuarioId;
    private final ListaEnlazada<Transaccion> historialTransacciones;
    private int totalTransacciones;

    public Billetera(String nombre, TipoBilletera tipo, double saldoInicial, String usuarioId) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = saldoInicial;
        this.activa = true;
        this.usuarioId = usuarioId;
        this.historialTransacciones = new ListaEnlazada<>();
    }

    public boolean recargar(double monto) {
        if (monto <= 0) return false;
        this.saldo += monto; return true;
    }

    public boolean retirar(double monto) {
        if (monto <= 0 || monto > saldo) return false;
        this.saldo -= monto; return true;
    }

    public void agregarTransaccion(Transaccion t) {
        historialTransacciones.agregar(t);
        totalTransacciones++;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoBilletera getTipo() { return tipo; }
    public double getSaldo() { return saldo; }
    public boolean isActiva() { return activa; }
    public String getUsuarioId() { return usuarioId; }
    public ListaEnlazada<Transaccion> getHistorialTransacciones() { return historialTransacciones; }
    public int getTotalTransacciones() { return totalTransacciones; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    @Override
    public String toString() {
        return String.format("Billetera[%s] '%s' | %s | $%.2f | Activa:%s | Txs:%d",
                id, nombre, tipo, saldo, activa, totalTransacciones);
    }
}