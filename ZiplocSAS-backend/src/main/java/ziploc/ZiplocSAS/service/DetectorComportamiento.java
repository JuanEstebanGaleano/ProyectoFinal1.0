package ziploc.ZiplocSAS.service;

import org.springframework.stereotype.Service;
import ziploc.ZiplocSAS.model.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DetectorComportamiento {

    private final Map<String, List<Transaccion>> historial = new HashMap<>();
    private static final int    VENTANA_MIN = 5, LIMITE_TX = 3;
    private static final double FACTOR      = 3.0;

    public void analizar(Usuario usuario, Billetera billetera, Transaccion tx) {
        String uid = usuario.getId();
        historial.putIfAbsent(uid, new ArrayList<>());
        List<Transaccion> rec = historial.get(uid);
        rec.add(tx);
        LocalDateTime lim = tx.getFecha().minusMinutes(VENTANA_MIN);
        rec.removeIf(t -> t.getFecha().isBefore(lim));

        boolean    sospechosa = false;
        NivelRiesgo riesgo    = NivelRiesgo.BAJO;

        // ── Muchas transferencias rápidas ────────────────────────────────────
        long txRapidas = rec.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.TRANSFERENCIA_ENVIADA)
                .count();
        if (txRapidas >= LIMITE_TX) {
            sospechosa = true; riesgo = NivelRiesgo.MEDIO;
            usuario.agregarNotificacion(
                    "🚨 " + txRapidas + " transferencias en " + VENTANA_MIN + " min");
        }

        // ── Monto inusual vs. promedio histórico ─────────────────────────────
        double prom = calcularPromedio(usuario);
        if (prom > 0 && tx.getValor() > prom * FACTOR) {
            sospechosa = true; riesgo = NivelRiesgo.ALTO;
            usuario.agregarNotificacion(
                    String.format("🚨 Monto inusual: $%.2f (prom $%.2f)", tx.getValor(), prom));
        }

        // ── Retiro masivo con saldo bajo ─────────────────────────────────────
        if (tx.getTipo() == TipoTransaccion.RETIRO
                && billetera.getSaldo() < 10 && tx.getValor() > 500) {
            sospechosa = true; riesgo = NivelRiesgo.ALTO;
            usuario.agregarNotificacion("🚨 Retiro masivo en " + billetera.getNombre());
        }

        // ── Actividad nocturna ────────────────────────────────────────────────
        int hora = tx.getFecha().getHour();
        if ((hora >= 23 || hora <= 5) && tx.getValor() > 200) {
            sospechosa = true;
            if (riesgo.ordinal() < NivelRiesgo.MEDIO.ordinal()) riesgo = NivelRiesgo.MEDIO;
            usuario.agregarNotificacion("🚨 Actividad nocturna inusual: " + hora + "h");
        }

        if (sospechosa) {
            tx.setSospechosa(true);
            tx.setNivelRiesgo(riesgo);
            usuario.agregarAuditoria(tx);
            System.out.println("⚠️ ALERTA [" + riesgo + "]: Tx " + tx.getId() + " marcada sospechosa.");
        }
    }

    private double calcularPromedio(Usuario u) {
        double total = 0; int c = 0;
        for (Billetera b : u.getBilleteras())
            for (Transaccion t : b.getHistorialTransacciones()) { total += t.getValor(); c++; }
        return c == 0 ? 0 : total / c;
    }
}