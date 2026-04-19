package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.OperacionProgramadaRepository;
import ziploc.ZiplocSAS.structures.ColaPrioridad;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperacionProgramadaService {

    private final OperacionProgramadaRepository opRepo;
    private final TransaccionService txService;
    private final UsuarioService usuarioService;

    // Cola de prioridad (Min-Heap) para ordenar por fecha de ejecución
    private final ColaPrioridad<OperacionProgramada> cola = new ColaPrioridad<>();

    @Transactional
    public OperacionProgramada programar(String uid, String bO, String bD,
                                         TipoTransaccion tipo, double monto,
                                         LocalDateTime fecha, String desc) {
        OperacionProgramada op = new OperacionProgramada(uid, bO, bD, tipo, monto, fecha, desc);
        opRepo.save(op);
        cola.insertar(op);
        usuarioService.notificar(uid, "📅 Programada: " + desc + " para " + fecha.toString().substring(0, 10));
        return op;
    }

    /** Se ejecuta automáticamente cada 60 segundos */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public int ejecutarPendientes() {
        List<OperacionProgramada> pendientes = opRepo.findPendientesAEjecutar(LocalDateTime.now());
        int count = 0;
        for (OperacionProgramada op : pendientes) {
            try {
                ejecutar(op);
                op.marcarEjecutada();
                opRepo.save(op);
                usuarioService.notificar(op.getUsuarioId(), "✅ Ejecutada: " + op.getDescripcion());
                count++;
            } catch (Exception e) {
                usuarioService.notificar(op.getUsuarioId(), "❌ Falló: " + op.getDescripcion());
            }
        }
        return count;
    }

    /** Fuerza ejecución manual */
    @Transactional
    public int ejecutarManual() { return ejecutarPendientes(); }

    private void ejecutar(OperacionProgramada op) {
        switch (op.getTipo()) {
            case RECARGA -> txService.recargar(op.getUsuarioId(), op.getBilleteraDestinoId(), op.getMonto());
            case RETIRO  -> txService.retirar(op.getUsuarioId(), op.getBilleteraOrigenId(), op.getMonto());
            default      -> txService.transferir(op.getUsuarioId(), op.getBilleteraOrigenId(), op.getBilleteraDestinoId(), op.getMonto());
        }
    }

    public List<OperacionProgramada> listarPorUsuario(String uid) { return opRepo.findByUsuarioId(uid); }
    public List<OperacionProgramada> listarPendientes() { return opRepo.findByEjecutadaFalse(); }
    public int totalEnCola() { return cola.size(); }
}