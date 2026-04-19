package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.OperacionProgramadaRepository;
import ziploc.ZiplocSAS.structures.ColaPrioridad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperacionProgramadaService {

    private final OperacionProgramadaRepository opRepo;
    private final TransaccionService txService;

    // Cola de prioridad en memoria (min-heap por fecha)
    private final ColaPrioridad<OperacionProgramada> cola = new ColaPrioridad<>();

    // ─── PROGRAMAR ────────────────────────────────────────────────────────────
    @Transactional
    public OperacionProgramada programar(String usuarioId, String bOrigen, String bDestino,
                                         TipoTransaccion tipo, double monto,
                                         LocalDateTime fecha, String descripcion) {
        OperacionProgramada op = new OperacionProgramada(
                usuarioId, bOrigen, bDestino, tipo, monto, fecha, descripcion);
        opRepo.save(op);
        cola.insertar(op);
        return op;
    }

    // ─── EJECUTAR PENDIENTES ──────────────────────────────────────────────────
    @Transactional
    public int ejecutarOperacionesPendientes() {
        // Cargar desde BD las que aún no se han ejecutado y ya vencieron
        List<OperacionProgramada> pendientes = opRepo.findPendientesAEjecutar(LocalDateTime.now());
        int ejecutadas = 0;

        for (OperacionProgramada op : pendientes) {
            try {
                ejecutar(op);
                op.marcarEjecutada();
                opRepo.save(op);
                ejecutadas++;
            } catch (Exception e) {
                System.err.println("⚠️ Error ejecutando operación [" + op.getId() + "]: " + e.getMessage());
            }
        }

        // Limpiar cola en memoria sincronizando con BD
        sincronizarCola();

        return ejecutadas;
    }

    // ─── LISTAR POR USUARIO ───────────────────────────────────────────────────
    public List<OperacionProgramada> listarPorUsuario(String usuarioId) {
        return opRepo.findByUsuarioId(usuarioId);
    }

    // ─── LISTAR PENDIENTES ────────────────────────────────────────────────────
    public List<OperacionProgramada> listarPendientes() {
        return opRepo.findByEjecutadaFalse();
    }

    // ─── TOTAL PENDIENTES ─────────────────────────────────────────────────────
    public int totalPendientes() {
        return opRepo.findByEjecutadaFalse().size();
    }

    // ─── Auxiliar: ejecutar una operación ────────────────────────────────────
    private void ejecutar(OperacionProgramada op) {
        switch (op.getTipo()) {
            case RECARGA ->
                    txService.recargar(op.getUsuarioId(), op.getBilleteraDestinoId(), op.getMonto());
            case RETIRO ->
                    txService.retirar(op.getUsuarioId(), op.getBilleteraOrigenId(), op.getMonto());
            case TRANSFERENCIA_ENVIADA, PAGO_PROGRAMADO ->
                    txService.transferir(op.getUsuarioId(),
                            op.getBilleteraOrigenId(),
                            op.getBilleteraDestinoId(),
                            op.getMonto());
            default ->
                    throw new IllegalArgumentException("Tipo no ejecutable automáticamente: " + op.getTipo());
        }
    }

    // ─── Auxiliar: sincronizar cola en memoria con BD ─────────────────────────
    private void sincronizarCola() {
        // Vaciar cola y recargar solo pendientes no ejecutados
        while (!cola.isEmpty()) cola.extraer();
        opRepo.findByEjecutadaFalse().forEach(cola::insertar);
    }
}