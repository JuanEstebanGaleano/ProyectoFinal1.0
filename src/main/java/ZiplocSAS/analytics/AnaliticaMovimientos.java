package ZiplocSAS.analytics;


import ZiplocSAS.model.*;
import ZiplocSAS.service.GestorUsuarios;
import java.time.LocalDateTime;
import java.util.*;

public class AnaliticaMovimientos {

    private final GestorUsuarios gu;

    public AnaliticaMovimientos(GestorUsuarios gu) { this.gu = gu; }

    public Usuario usuarioMasActivo() {
        Usuario mejor = null; int max = 0;
        for (Usuario u : gu.getTodosUsuarios()) {
            int t = 0;
            for (Billetera b : u.getBilleteras()) t += b.getTotalTransacciones();
            if (t > max) { max = t; mejor = u; }
        }
        return mejor;
    }

    public Billetera billeteraConMayorUso() {
        Billetera mejor = null; int max = 0;
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                if (b.getTotalTransacciones() > max) { max = b.getTotalTransacciones(); mejor = b; }
        return mejor;
    }

    public double montoTotalEnRango(LocalDateTime desde, LocalDateTime hasta) {
        double total = 0;
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                for (Transaccion t : b.getHistorialTransacciones())
                    if (!t.getFecha().isBefore(desde) && !t.getFecha().isAfter(hasta)
                            && t.getEstado() == EstadoTransaccion.COMPLETADA) total += t.getValor();
        return total;
    }

    public Map<TipoTransaccion, Integer> frecuenciaPorTipo() {
        Map<TipoTransaccion, Integer> m = new EnumMap<>(TipoTransaccion.class);
        for (TipoTransaccion t : TipoTransaccion.values()) m.put(t, 0);
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras())
                for (Transaccion t : b.getHistorialTransacciones()) m.merge(t.getTipo(), 1, Integer::sum);
        return m;
    }

    public Map<TipoBilletera, Integer> actividadPorCategoria() {
        Map<TipoBilletera, Integer> m = new EnumMap<>(TipoBilletera.class);
        for (TipoBilletera t : TipoBilletera.values()) m.put(t, 0);
        for (Usuario u : gu.getTodosUsuarios())
            for (Billetera b : u.getBilleteras()) m.merge(b.getTipo(), b.getTotalTransacciones(), Integer::sum);
        return m;
    }

    public List<Usuario> usuariosNivelAlto() { return gu.buscarUsuariosPorRangoPuntos(1001, Integer.MAX_VALUE); }

    public void imprimirReporteGeneral() {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("         📊 REPORTE GENERAL DEL SISTEMA");
        System.out.println("=".repeat(55));
        System.out.println("👥 Total usuarios: " + gu.totalUsuarios());
        Usuario ma = usuarioMasActivo();
        if (ma != null) System.out.println("🏆 Más activo: " + ma.getNombre());
        Billetera mb = billeteraConMayorUso();
        if (mb != null) System.out.println("💼 Billetera top: " + mb.getNombre() + " (" + mb.getTotalTransacciones() + " txs)");
        System.out.println("\n📈 Frecuencia por tipo:");
        frecuenciaPorTipo().forEach((k, v) -> System.out.printf("   %-30s: %d%n", k, v));
        System.out.println("\n📂 Actividad por categoría:");
        actividadPorCategoria().forEach((k, v) -> System.out.printf("   %-20s: %d txs%n", k, v));
        System.out.println("\n🏅 Usuarios por puntos (BST inorden):");
        gu.obtenerUsuariosOrdenadosPorPuntos().forEach(u ->
                System.out.printf("   %s | %d pts | %s%n", u.getNombre(), u.getPuntosTotales(), u.getNivel()));
        System.out.println("=".repeat(55));
    }
}