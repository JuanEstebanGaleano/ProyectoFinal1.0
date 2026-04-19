package ziploc.ZiplocSAS.model;


import ziploc.ZiplocSAS.structures.ListaEnlazada;
import ziploc.ZiplocSAS.structures.Pila;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "usuarios")
@Data @NoArgsConstructor
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

    public Usuario(String nombre, String email, String cedula) {
        this.nombre = nombre;
        this.email = email;
        this.cedula = cedula;
    }

    public void sumarPuntos(int pts) {
        this.puntosTotales += pts;
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
    }

    public void descontarPuntos(int pts) {
        this.puntosTotales = Math.max(0, this.puntosTotales - pts);
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
    }
}

public class Usuario {
    private final String id;
    private String nombre, email, cedula;
    private int puntosTotales;
    private NivelUsuario nivel;
    private final ListaEnlazada<Billetera> billeteras;
    private final Pila<Transaccion> pilaReversiones;
    private final ListaEnlazada<String> notificaciones;
    private final ListaEnlazada<Transaccion> auditoriaSospechosas;

    public Usuario(String nombre, String email, String cedula) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.nombre = nombre; this.email = email; this.cedula = cedula;
        this.puntosTotales = 0; this.nivel = NivelUsuario.BRONCE;
        this.billeteras = new ListaEnlazada<>();
        this.pilaReversiones = new Pila<>();
        this.notificaciones = new ListaEnlazada<>();
        this.auditoriaSospechosas = new ListaEnlazada<>();
    }

    public void agregarBilletera(Billetera b) { billeteras.agregar(b); }

    public Billetera buscarBilletera(String id) {
        for (int i = 0; i < billeteras.size(); i++) {
            Billetera b = billeteras.obtener(i);
            if (b.getId().equals(id)) return b;
        }
        return null;
    }

    public void sumarPuntos(int pts) {
        this.puntosTotales += pts;
        NivelUsuario anterior = nivel;
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
        if (!nivel.equals(anterior)) agregarNotificacion("¡Subiste al nivel " + nivel + "! 🎉");
    }

    public void descontarPuntos(int pts) {
        this.puntosTotales = Math.max(0, this.puntosTotales - pts);
        this.nivel = NivelUsuario.calcularNivel(puntosTotales);
    }

    public void agregarNotificacion(String msg) {
        notificaciones.agregarAlInicio(msg);
        if (notificaciones.size() > 20) notificaciones.eliminarUltimo();
    }

    public void registrarTransaccionReversible(Transaccion t) { pilaReversiones.push(t); }
    public Transaccion obtenerUltimaReversible() { return pilaReversiones.isEmpty() ? null : pilaReversiones.pop(); }
    public void agregarAuditoria(Transaccion t) { auditoriaSospechosas.agregar(t); }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getCedula() { return cedula; }
    public int getPuntosTotales() { return puntosTotales; }
    public NivelUsuario getNivel() { return nivel; }
    public ListaEnlazada<Billetera> getBilleteras() { return billeteras; }
    public Pila<Transaccion> getPilaReversiones() { return pilaReversiones; }
    public ListaEnlazada<String> getNotificaciones() { return notificaciones; }
    public ListaEnlazada<Transaccion> getAuditoriaSospechosas() { return auditoriaSospechosas; }
    public void setNombre(String n) { this.nombre = n; }
    public void setEmail(String e) { this.email = e; }

    @Override
    public String toString() {
        return String.format("Usuario[%s] %s | %s | Pts:%d | Nivel:%s | Billeteras:%d",
                id, nombre, email, puntosTotales, nivel, billeteras.size());
    }
}