package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.structures.GrafoTransferencias;

public class GestorTransacciones {

    private final GestorUsuarios gu;
    private final GestorBilleteras gb;
    private final GrafoTransferencias grafo = new GrafoTransferencias();
    private final DetectorComportamiento detector = new DetectorComportamiento();

    public GestorTransacciones(GestorUsuarios gu, GestorBilleteras gb) {
        this.gu = gu; this.gb = gb;
    }

    public boolean recargar(String uid, String bid, double monto) {
        Usuario u = gu.buscarPorId(uid); Billetera b = gb.buscarPorId(bid);
        if (u == null || b == null || !b.isActiva() || !b.getUsuarioId().equals(uid)) {
            System.out.println("❌ Datos inválidos."); return false;
        }
        b.recargar(monto);
        Transaccion tx = new Transaccion(TipoTransaccion.RECARGA, monto, null, bid);
        b.agregarTransaccion(tx); u.registrarTransaccionReversible(tx);
        int pts = u.getPuntosTotales(); u.sumarPuntos(tx.getPuntosGenerados());
        gu.actualizarArbolPuntos(u, pts); detector.analizar(u, b, tx);
        if (b.getSaldo() < 50) u.agregarNotificacion("⚠️ Saldo bajo en " + b.getNombre());
        System.out.printf("✅ Recarga $%.2f en '%s'. Saldo: $%.2f | +%d pts%n",
                monto, b.getNombre(), b.getSaldo(), tx.getPuntosGenerados());
        return true;
    }

    public boolean retirar(String uid, String bid, double monto) {
        Usuario u = gu.buscarPorId(uid); Billetera b = gb.buscarPorId(bid);
        if (u == null || b == null || !b.isActiva()) { System.out.println("❌ Datos inválidos."); return false; }
        if (!b.retirar(monto)) {
            System.out.println("❌ Saldo insuficiente: $" + b.getSaldo());
            u.agregarNotificacion("Retiro rechazado en " + b.getNombre()); return false;
        }
        Transaccion tx = new Transaccion(TipoTransaccion.RETIRO, monto, bid, null);
        b.agregarTransaccion(tx); u.registrarTransaccionReversible(tx);
        int pts = u.getPuntosTotales(); u.sumarPuntos(tx.getPuntosGenerados());
        gu.actualizarArbolPuntos(u, pts); detector.analizar(u, b, tx);
        if (b.getSaldo() < 50) u.agregarNotificacion("⚠️ Saldo bajo en " + b.getNombre());
        System.out.printf("✅ Retiro $%.2f de '%s'. Saldo: $%.2f | +%d pts%n",
                monto, b.getNombre(), b.getSaldo(), tx.getPuntosGenerados());
        return true;
    }

    public boolean transferir(String uidOrigen, String bidOrigen, String bidDestino, double monto) {
        Usuario uO = gu.buscarPorId(uidOrigen);
        Billetera bO = gb.buscarPorId(bidOrigen), bD = gb.buscarPorId(bidDestino);
        if (uO == null || bO == null || bD == null || !bO.isActiva() || !bD.isActiva()) {
            System.out.println("❌ Datos inválidos."); return false;
        }
        if (!bO.retirar(monto)) {
            System.out.println("❌ Saldo insuficiente: $" + bO.getSaldo()); return false;
        }
        bD.recargar(monto);
        Transaccion txE = new Transaccion(TipoTransaccion.TRANSFERENCIA_ENVIADA, monto, bidOrigen, bidDestino);
        Transaccion txR = new Transaccion(TipoTransaccion.TRANSFERENCIA_RECIBIDA, monto, bidOrigen, bidDestino);
        bO.agregarTransaccion(txE); bD.agregarTransaccion(txR);
        uO.registrarTransaccionReversible(txE);
        int pts = uO.getPuntosTotales(); uO.sumarPuntos(txE.getPuntosGenerados());
        gu.actualizarArbolPuntos(uO, pts);
        if (!bD.getUsuarioId().equals(uidOrigen)) {
            Usuario uD = gu.buscarPorId(bD.getUsuarioId());
            if (uD != null) {
                int ptD = uD.getPuntosTotales(); uD.sumarPuntos(txR.getPuntosGenerados());
                gu.actualizarArbolPuntos(uD, ptD);
                grafo.agregarTransferencia(uidOrigen, uD.getId(), monto);
            }
        }
        detector.analizar(uO, bO, txE);
        System.out.printf("✅ Transferencia $%.2f: '%s' -> '%s' | +%d pts%n",
                monto, bO.getNombre(), bD.getNombre(), txE.getPuntosGenerados());
        return true;
    }

    public boolean revertirUltimaOperacion(String uid) {
        Usuario u = gu.buscarPorId(uid);
        if (u == null) return false;
        Transaccion tx = u.obtenerUltimaReversible();
        if (tx == null) { System.out.println("❌ Sin operaciones para revertir."); return false; }
        if (tx.getEstado() == EstadoTransaccion.REVERTIDA) { System.out.println("❌ Ya revertida."); return false; }
        if (tx.getTipo() == TipoTransaccion.RECARGA) {
            Billetera b = gb.buscarPorId(tx.getBilleteraDestinoId());
            if (b != null) b.retirar(tx.getValor());
        } else if (tx.getTipo() == TipoTransaccion.RETIRO) {
            Billetera b = gb.buscarPorId(tx.getBilleteraOrigenId());
            if (b != null) b.recargar(tx.getValor());
        } else if (tx.getTipo() == TipoTransaccion.TRANSFERENCIA_ENVIADA) {
            Billetera bO = gb.buscarPorId(tx.getBilleteraOrigenId());
            Billetera bD = gb.buscarPorId(tx.getBilleteraDestinoId());
            if (bO != null) bO.recargar(tx.getValor());
            if (bD != null) bD.retirar(tx.getValor());
        }
        int pts = u.getPuntosTotales(); u.descontarPuntos(tx.getPuntosGenerados());
        gu.actualizarArbolPuntos(u, pts); tx.revertir();
        u.agregarNotificacion("Operación revertida: " + tx.getId());
        System.out.printf("✅ Operación [%s] revertida. Puntos: %d%n", tx.getId(), u.getPuntosTotales());
        return true;
    }

    public GrafoTransferencias getGrafo() { return grafo; }
    public DetectorComportamiento getDetector() { return detector; }
}