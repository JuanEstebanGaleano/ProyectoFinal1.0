package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.dto.response.GrafoResponse;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.*;
import ziploc.ZiplocSAS.structures.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository txRepo;
    private final BilleteraRepository billeteraRepo;
    private final UsuarioService usuarioService;

    // Estructuras en memoria
    private final GrafoTransferencias grafo = new GrafoTransferencias();
    private final Map<String, Pila<String>> pilasReversiones = new HashMap<>();

    // ─── RECARGAR ────────────────────────────────────────────────────────────
    @Transactional
    public Transaccion recargar(String usuarioId, String billeteraId, double monto) {
        Billetera b = billeteraRepo.findById(billeteraId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera no encontrada: " + billeteraId));
        if (!b.isActiva()) throw new IllegalStateException("Billetera inactiva");
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor a cero");

        b.recargar(monto);
        b.registrarTransaccion();
        billeteraRepo.save(b);

        Transaccion tx = new Transaccion(TipoTransaccion.RECARGA, monto, null, billeteraId, usuarioId);
        txRepo.save(tx);

        registrarEnPila(usuarioId, tx.getId());
        usuarioService.sumarPuntos(usuarioId, tx.getPuntosGenerados());

        if (b.getSaldo() < 50)
            usuarioService.notificar(usuarioId, "⚠️ Saldo bajo en " + b.getNombre());

        return tx;
    }

    // ─── RETIRAR ─────────────────────────────────────────────────────────────
    @Transactional
    public Transaccion retirar(String usuarioId, String billeteraId, double monto) {
        Billetera b = billeteraRepo.findById(billeteraId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera no encontrada: " + billeteraId));
        if (!b.isActiva()) throw new IllegalStateException("Billetera inactiva");
        if (!b.retirar(monto)) {
            usuarioService.notificar(usuarioId, "Retiro rechazado: saldo insuficiente en " + b.getNombre());
            throw new IllegalStateException("Saldo insuficiente: $" + b.getSaldo());
        }

        b.registrarTransaccion();
        billeteraRepo.save(b);

        Transaccion tx = new Transaccion(TipoTransaccion.RETIRO, monto, billeteraId, null, usuarioId);
        txRepo.save(tx);

        registrarEnPila(usuarioId, tx.getId());
        usuarioService.sumarPuntos(usuarioId, tx.getPuntosGenerados());

        if (b.getSaldo() < 50)
            usuarioService.notificar(usuarioId, "⚠️ Saldo bajo en " + b.getNombre());

        return tx;
    }

    // ─── TRANSFERIR ──────────────────────────────────────────────────────────
    @Transactional
    public Transaccion transferir(String usuarioOrigenId, String bOrigenId,
                                  String bDestinoId, double monto) {
        Billetera bO = billeteraRepo.findById(bOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera origen no encontrada"));
        Billetera bD = billeteraRepo.findById(bDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera destino no encontrada"));

        if (!bO.isActiva() || !bD.isActiva())
            throw new IllegalStateException("Una de las billeteras está inactiva");
        if (!bO.retirar(monto))
            throw new IllegalStateException("Saldo insuficiente: $" + bO.getSaldo());

        bD.recargar(monto);
        bO.registrarTransaccion();
        bD.registrarTransaccion();
        billeteraRepo.save(bO);
        billeteraRepo.save(bD);

        Transaccion txE = new Transaccion(TipoTransaccion.TRANSFERENCIA_ENVIADA,  monto, bOrigenId, bDestinoId, usuarioOrigenId);
        Transaccion txR = new Transaccion(TipoTransaccion.TRANSFERENCIA_RECIBIDA, monto, bOrigenId, bDestinoId, bD.getUsuarioId());
        txRepo.save(txE);
        txRepo.save(txR);

        registrarEnPila(usuarioOrigenId, txE.getId());
        usuarioService.sumarPuntos(usuarioOrigenId, txE.getPuntosGenerados());

        // Puntos y grafo para usuario destino (si es diferente)
        if (!bD.getUsuarioId().equals(usuarioOrigenId)) {
            usuarioService.sumarPuntos(bD.getUsuarioId(), txR.getPuntosGenerados());
            grafo.agregarTransferencia(usuarioOrigenId, bD.getUsuarioId(), monto);
        }

        return txE;
    }

    // ─── REVERTIR ÚLTIMA OPERACIÓN ───────────────────────────────────────────
    @Transactional
    public void revertirUltimaOperacion(String usuarioId) {
        Pila<String> pila = pilasReversiones.get(usuarioId);
        if (pila == null || pila.isEmpty())
            throw new IllegalStateException("No hay operaciones para revertir del usuario: " + usuarioId);

        String txId = pila.pop();
        Transaccion tx = txRepo.findById(txId)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + txId));

        if (tx.getEstado() == EstadoTransaccion.REVERTIDA)
            throw new IllegalStateException("La transacción ya fue revertida: " + txId);

        // Deshacer el efecto sobre saldos
        switch (tx.getTipo()) {
            case RECARGA -> {
                Billetera b = billeteraRepo.findById(tx.getBilleteraDestinoId()).orElseThrow();
                b.retirar(tx.getValor());
                billeteraRepo.save(b);
            }
            case RETIRO -> {
                Billetera b = billeteraRepo.findById(tx.getBilleteraOrigenId()).orElseThrow();
                b.recargar(tx.getValor());
                billeteraRepo.save(b);
            }
            case TRANSFERENCIA_ENVIADA -> {
                Billetera bO = billeteraRepo.findById(tx.getBilleteraOrigenId()).orElseThrow();
                Billetera bD = billeteraRepo.findById(tx.getBilleteraDestinoId()).orElseThrow();
                bO.recargar(tx.getValor());
                bD.retirar(tx.getValor());
                billeteraRepo.save(bO);
                billeteraRepo.save(bD);
            }
            default -> throw new IllegalStateException("Tipo de transacción no reversible: " + tx.getTipo());
        }

        tx.revertir(); // establece estado=REVERTIDA y puntosGenerados=0
        txRepo.save(tx);

        usuarioService.descontarPuntos(usuarioId, tx.getPuntosGenerados());
        usuarioService.notificar(usuarioId, "Operación revertida: " + tx.getId());
    }

    // ─── HISTORIAL ───────────────────────────────────────────────────────────
    public List<Transaccion> historialPorUsuario(String usuarioId) {
        return txRepo.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    // ─── TRANSACCIONES SOSPECHOSAS ───────────────────────────────────────────
    public List<Transaccion> listarSospechosas() {
        return txRepo.findBySospechosaTrue();
    }

    // ─── GRAFO ───────────────────────────────────────────────────────────────
    public GrafoResponse obtenerGrafo() {
        return GrafoResponse.builder()
                .totalVertices(grafo.getTotalVertices())
                .totalAristas(grafo.getTotalAristas())
                .tieneCiclos(grafo.tieneCiclo())
                .frecuenciaTransferencias(grafo.getFrecuenciaTransferencias())
                .montosTotales(grafo.getMontosTotales())
                .vertices(new ArrayList<>(grafo.getVertices()))
                .build();
    }

    // ─── Auxiliar pila por usuario ────────────────────────────────────────────
    private void registrarEnPila(String usuarioId, String txId) {
        pilasReversiones.putIfAbsent(usuarioId, new Pila<>());
        pilasReversiones.get(usuarioId).push(txId);
    }
}