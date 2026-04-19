package ziploc.ZiplocSAS.model;

import jakarta.persistence.*;
import lombok.*;
import ziploc.ZiplocSAS.structures.*;
import java.util.UUID;

@Entity @Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor  // ← Reemplaza @Data por @Getter @Setter
public class Usuario {

    @Id
    private String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String cedula;

    private int puntosTotales = 0;

    @Enumerated(EnumType.STRING)
    private NivelUsuario nivel = NivelUsuario.BRONCE;

    @Transient private ListaEnlazada<Billetera>   billeteras           = new ListaEnlazada<>();
    @Transient private Pila<Transaccion>           pilaReversiones      = new Pila<>();
    @Transient private ListaEnlazada<String>       notificaciones       = new ListaEnlazada<>();
    @Transient private ListaEnlazada<Transaccion>  auditoriaSospechosas = new ListaEnlazada<>();

    public Usuario(String nombre, String email, String cedula) {
        this.nombre = nombre; this.email = email; this.cedula = cedula;
    }

    // ── Puntos ───────────────────────────────────────────────────────────────
    public void sumarPuntos(int pts) {
        this.puntosTotales += pts;
        NivelUsuario anterior = this.nivel;
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
        if (!this.nivel.equals(anterior))
            agregarNotificacion("¡Subiste al nivel " + this.nivel + "! 🎉");
    }

    public void descontarPuntos(int pts) {
        this.puntosTotales = Math.max(0, this.puntosTotales - pts);
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
    }

    // ── Billeteras ───────────────────────────────────────────────────────────
    public void agregarBilletera(Billetera b) {
        if (billeteras == null) billeteras = new ListaEnlazada<>();
        billeteras.agregar(b);
    }

    public Billetera buscarBilletera(String id) {
        if (billeteras == null) return null;
        for (int i = 0; i < billeteras.size(); i++) {
            Billetera b = billeteras.obtener(i);
            if (b.getId().equals(id)) return b;
        }
        return null;
    }

    public ListaEnlazada<Billetera> getBilleteras() {
        if (billeteras == null) billeteras = new ListaEnlazada<>();
        return billeteras;
    }

    // ── Reversiones ──────────────────────────────────────────────────────────
    public void registrarTransaccionReversible(Transaccion t) {
        if (pilaReversiones == null) pilaReversiones = new Pila<>();
        pilaReversiones.push(t);
    }

    public Transaccion obtenerUltimaReversible() {
        if (pilaReversiones == null || pilaReversiones.isEmpty()) return null;
        return pilaReversiones.pop();
    }

    public Pila<Transaccion> getPilaReversiones() {
        if (pilaReversiones == null) pilaReversiones = new Pila<>();
        return pilaReversiones;
    }

    // ── Notificaciones ────────────────────────────────────────────────────────
    public void agregarNotificacion(String msg) {
        if (notificaciones == null) notificaciones = new ListaEnlazada<>();
        notificaciones.agregarAlInicio(msg);
        if (notificaciones.size() > 20) notificaciones.eliminarUltimo();
    }

    public ListaEnlazada<String> getNotificaciones() {
        if (notificaciones == null) notificaciones = new ListaEnlazada<>();
        return notificaciones;
    }

    // ── Auditoría ─────────────────────────────────────────────────────────────
    public void agregarAuditoria(Transaccion t) {
        if (auditoriaSospechosas == null) auditoriaSospechosas = new ListaEnlazada<>();
        auditoriaSospechosas.agregar(t);
    }

    public ListaEnlazada<Transaccion> getAuditoriaSospechosas() {
        if (auditoriaSospechosas == null) auditoriaSospechosas = new ListaEnlazada<>();
        return auditoriaSospechosas;
    }

    // ── toString() LIMPIO ────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
                "Usuario[%s] %-20s | %-25s | CC: %-12s | Pts: %4d | Nivel: %-8s | Billeteras: %d",
                id, nombre, email, cedula, puntosTotales, nivel,
                billeteras != null ? billeteras.size() : 0
        );
    }
}