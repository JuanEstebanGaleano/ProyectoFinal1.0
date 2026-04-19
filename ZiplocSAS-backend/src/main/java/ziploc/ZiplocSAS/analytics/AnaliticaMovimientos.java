package ziploc.ZiplocSAS.analytics;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.*;
import ziploc.ZiplocSAS.service.UsuarioService;
import ziploc.ZiplocSAS.service.BilleteraService;
import ziploc.ZiplocSAS.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnaliticaMovimientos {

    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;
    private final TransaccionService txService;
    private final TransaccionRepository txRepo;
    private final BilleteraRepository billeteraRepo;

    /** Usuario con más transacciones en todas sus billeteras */
    public Usuario usuarioMasActivo() {
        return usuarioService.listarTodos().stream()
                .max(Comparator.comparingInt(u ->
                        billeteraRepo.findByUsuarioId(u.getId()).stream()
                                .mapToInt(Billetera::getTotalTransacciones).sum()))
                .orElse(null);
    }

    /** Billetera con más transacciones registradas */
    public Billetera billeteraConMayorUso() {
        return billeteraService.listarTodasOrdenadas().stream()
                .findFirst().orElse(null);
    }

    /** Suma de montos de transacciones COMPLETADAS en un rango de fechas */
    public double montoTotalEnRango(LocalDateTime desde, LocalDateTime hasta) {
        Double result = txRepo.sumMontoEnRango(desde, hasta);
        return result != null ? result : 0.0;
    }

    /** Frecuencia de cada tipo de transacción */
    public Map<TipoTransaccion, Integer> frecuenciaPorTipo() {
        Map<TipoTransaccion, Integer> mapa = new EnumMap<>(TipoTransaccion.class);
        for (TipoTransaccion t : TipoTransaccion.values()) mapa.put(t, 0);
        txRepo.countByTipo().forEach(row ->
                mapa.put((TipoTransaccion) row[0], ((Long) row[1]).intValue()));
        return mapa;
    }

    /** Total de transacciones agrupadas por tipo de billetera */
    public Map<TipoBilletera, Integer> actividadPorCategoria() {
        Map<TipoBilletera, Integer> mapa = new EnumMap<>(TipoBilletera.class);
        for (TipoBilletera t : TipoBilletera.values()) mapa.put(t, 0);
        billeteraRepo.findAll().forEach(b ->
                mapa.merge(b.getTipo(), b.getTotalTransacciones(), Integer::sum));
        return mapa;
    }

    /** Usuarios con nivel ORO o PLATINO (puntos > 1000) */
    public List<Usuario> usuariosNivelAlto() {
        return usuarioService.buscarPorRangoPuntos(1001, Integer.MAX_VALUE);
    }

    /** Imprime un reporte en consola (útil para logs/debug) */
    public void imprimirReporteGeneral() {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("         📊 REPORTE GENERAL DEL SISTEMA");
        System.out.println("=".repeat(55));
        System.out.println("👥 Total usuarios: " + usuarioService.total());

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
        usuarioService.listarOrdenadosPorPuntos().forEach(u ->
                System.out.printf("   %s | %d pts | %s%n",
                        u.getNombre(), u.getPuntosTotales(), u.getNivel()));

        System.out.println("=".repeat(55));
    }
}