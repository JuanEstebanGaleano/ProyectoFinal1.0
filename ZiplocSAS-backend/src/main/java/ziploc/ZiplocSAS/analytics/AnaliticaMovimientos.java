package ziploc.ZiplocSAS.analytics;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.service.GestorUsuarios;

import java.time.LocalDateTime;
import java.util.*;

public class AnaliticaMovimientos {

    private final GestorUsuarios gu;

    // ── Constructor para módulo de CONSOLA ───────────────────────────────────
    public AnaliticaMovimientos(GestorUsuarios gu) {
        this.gu = gu;
    }

    // ── Usuario con más transacciones ────────────────────────────────────────
    public Usuario usuarioMasActivo() {
        Usuario mejor = null;
        int max = 0;
        for (Usuario u : gu.getTodosUsuarios()) {
            int t = 0;
            for (Billetera b : u.getBilleteras())
                t += b.getTotalTransacciones();
            if (t > max) { max = t; mejor = u; }
        }
        return mejor;
    }

    // ── Billetera con más transacciones ──────────────────────────────────────
    public Billetera billeteraConMayorUso() {
        Billetera mejor = null;
        int max = 0;
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                if (b.getTotalTransacciones() > max) { max = b.getTotalTransacciones(); mejor = b; }
        return mejor;
    }

    // ── Monto total de transacciones COMPLETADAS en rango de fechas ───────────
    public double montoTotalEnRango(LocalDateTime desde, LocalDateTime hasta) {
        double total = 0;
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                for (Transaccion t : b.getHistorialTransacciones())
                    if (!t.getFecha().isBefore(desde) && !t.getFecha().isAfter(hasta)
                            && t.getEstado() == EstadoTransaccion.COMPLETADA)
                        total += t.getValor();
        return total;
    }

    // ── Frecuencia por tipo de transacción ───────────────────────────────────
    public Map<TipoTransaccion, Integer> frecuenciaPorTipo() {
        Map<TipoTransaccion, Integer> m = new EnumMap<>(TipoTransaccion.class);
        for (TipoTransaccion t : TipoTransaccion.values()) m.put(t, 0);
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                for (Transaccion t : b.getHistorialTransacciones())
                    m.merge(t.getTipo(), 1, Integer::sum);
        return m;
    }

    // ── Actividad por categoría de billetera ─────────────────────────────────
    public Map<TipoBilletera, Integer> actividadPorCategoria() {
        Map<TipoBilletera, Integer> m = new EnumMap<>(TipoBilletera.class);
        for (TipoBilletera t : TipoBilletera.values()) m.put(t, 0);
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                m.merge(b.getTipo(), b.getTotalTransacciones(), Integer::sum);
        return m;
    }

    // ── Usuarios nivel ORO o PLATINO (puntos > 1000) ─────────────────────────
    public List<Usuario> usuariosNivelAlto() {
        return gu.buscarUsuariosPorRangoPuntos(1001, Integer.MAX_VALUE);
    }

    // ── Transacciones sospechosas de todos los usuarios ───────────────────────
    public List<Transaccion> listarSospechosas() {
        List<Transaccion> resultado = new ArrayList<>();
        for (Usuario u : gu.getTodosUsuarios())
            for (Transaccion t : u.getAuditoriaSospechosas())
                resultado.add(t);
        return resultado;
    }

    // ── Reporte general en consola ────────────────────────────────────────────
    public void imprimirReporteGeneral() {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("         📊 REPORTE GENERAL DEL SISTEMA");
        System.out.println("=".repeat(55));
        System.out.println("👥 Total usuarios: " + gu.totalUsuarios());

        Usuario ma = usuarioMasActivo();
        if (ma != null) System.out.println("🏆 Más activo: " + ma.getNombre());

        Billetera mb = billeteraConMayorUso();
        if (mb != null) System.out.printf("💼 Billetera top: %s (%d txs)%n",
                mb.getNombre(), mb.getTotalTransacciones());

        System.out.println("\n📈 Frecuencia por tipo:");
        frecuenciaPorTipo().forEach((k, v) ->
                System.out.printf("   %-30s: %d%n", k, v));

        System.out.println("\n📂 Actividad por categoría:");
        actividadPorCategoria().forEach((k, v) ->
                System.out.printf("   %-20s: %d txs%n", k, v));

        System.out.println("\n🏅 Usuarios por puntos (BST inorden):");
        gu.obtenerUsuariosOrdenadosPorPuntos().forEach(u ->
                System.out.printf("   %-20s | %4d pts | %s%n",
                        u.getNombre(), u.getPuntosTotales(), u.getNivel()));

        System.out.println("\n⚠️  Transacciones sospechosas: " + listarSospechosas().size());

        double montoHoy = montoTotalEnRango(
                LocalDateTime.now().withHour(0).withMinute(0),
                LocalDateTime.now());
        System.out.printf("\n💰 Monto movido hoy: $%.2f%n", montoHoy);

        List<Usuario> altos = usuariosNivelAlto();
        System.out.println("\n🥇 Usuarios nivel ORO/PLATINO: " + altos.size());
        altos.forEach(u -> System.out.printf("   %s | %d pts | %s%n",
                u.getNombre(), u.getPuntosTotales(), u.getNivel()));

        System.out.println("=".repeat(55));
    }
}