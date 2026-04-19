package ziploc.ZiplocSAS.service;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.TransaccionRepository;
import ziploc.ZiplocSAS.structures.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository txRepo;
    private final BilleteraService billeteraService;
    private final UsuarioService usuarioService;

    // ── Estructuras en memoria ────────────────────────────────────────────
    private final GrafoTransferencias grafo = new GrafoTransferencias();
    private final Map<String, Pila<String>> pilasReversiones = new HashMap<>();

    @Transactional
    public Transaccion recargar(String uid, String bid, double monto) {
        Billetera b = billeteraService.recargar(bid, monto);
        Transaccion tx = new Transaccion(TipoTransaccion.RECARGA, monto, null, bid, uid);
        txRepo.save(tx);
        apilarReversion(uid, tx.getId());
        usuarioService.sumarPuntos(uid, tx.getPuntosGenerados());
        if (b.getSaldo() < 50)
            usuarioService.notificar(uid, "⚠️ Saldo bajo en " + b.getNombre() + ": $" + String.format("%.2f", b.getSaldo()));
        return tx;
    }

    @Transactional
    public Transaccion retirar(String uid, String bid, double monto) {
        Billetera b = billeteraService.retirar(bid, monto);
        Transaccion tx = new Transaccion(TipoTransaccion.RETIRO, monto, bid, null, uid);
        txRepo.save(tx);
        apilarReversion(uid, tx.getId());
        usuarioService.sumarPuntos(uid, tx.getPuntosGenerados());
        if (b.getSaldo() < 50)
            usuarioService.notificar(uid, "⚠️ Saldo bajo en " + b.getNombre());
        return tx;
    }

    @Transactional
    public Transaccion transferir(String uidOrigen, String bOrigenId, String bDestinoId, double monto) {
        Billetera bD = billeteraService.buscarPorId(bDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera destino no encontrada"));

        billeteraService.retirar(bOrigenId, monto);
        billeteraService.recargar(bDestinoId, monto);

        Transaccion txE = new Transaccion(TipoTransaccion.TRANSFERENCIA_ENVIADA, monto, bOrigenId, bDestinoId, uidOrigen);
        Transaccion txR = new Transaccion(TipoTransaccion.TRANSFERENCIA_RECIBIDA, monto, bOrigenId, bDestinoId, bD.getUsuarioId());
        txRepo.save(txE);
        txRepo.save(txR);
        apilarReversion(uidOrigen, txE.getId());
        usuarioService.sumarPuntos(uidOrigen, txE.getPuntosGenerados());

        if (!bD.getUsuarioId().equals(uidOrigen)) {
            usuarioService.sumarPuntos(bD.getUsuarioId(), txR.getPuntosGenerados());
            grafo.agregarTransferencia(uidOrigen, bD.getUsuarioId(), monto);
        }
        return txE;
    }

    @Transactional
    public Transaccion revertir(String uid) {
        Pila<String> pila = pilasReversiones.get(uid);
        if (pila == null || pila.isEmpty())
            throw new IllegalStateException("No hay operaciones para revertir");

        Transaccion tx = txRepo.findById(pila.pop())
                .orElseThrow(() -> new IllegalStateException("Transacción no encontrada"));
        if (tx.getEstado() == EstadoTransaccion.REVERTIDA)
            throw new IllegalStateException("La transacción ya fue revertida");

        switch (tx.getTipo()) {
            case RECARGA -> billeteraService.retirar(tx.getBilleteraDestinoId(), tx.getValor());
            case RETIRO  -> billeteraService.recargar(tx.getBilleteraOrigenId(), tx.getValor());
            case TRANSFERENCIA_ENVIADA -> {
                billeteraService.recargar(tx.getBilleteraOrigenId(), tx.getValor());
                billeteraService.retirar(tx.getBilleteraDestinoId(), tx.getValor());
            }
            default -> {}
        }

        usuarioService.descontarPuntos(uid, tx.getPuntosGenerados());
        tx.revertir();
        txRepo.save(tx);
        usuarioService.notificar(uid, "↩️ Operación revertida: " + tx.getId());
        return tx;
    }

    private void apilarReversion(String uid, String txId) {
        pilasReversiones.computeIfAbsent(uid, k -> new Pila<>()).push(txId);
    }

    public List<Transaccion> historialPorUsuario(String uid) { return txRepo.findByUsuarioIdOrderByFechaDesc(uid); }
    public List<Transaccion> historialPorBilletera(String bid) { return txRepo.findByBilleteraOrigenIdOrBilleteraDestinoId(bid, bid); }
    public List<Transaccion> obtenerSospechosas() { return txRepo.findBySospechosaTrue(); }
    public List<Transaccion> obtenerPorValorDesc() { return txRepo.findAllOrderByValorDesc(); }
    public Double montoEnRango(LocalDateTime desde, LocalDateTime hasta) { return txRepo.sumMontoEnRango(desde, hasta); }
    public List<Object[]> frecuenciaPorTipo() { return txRepo.countByTipo(); }
    public GrafoTransferencias getGrafo() { return grafo; }
    public long total() { return txRepo.count(); }
}