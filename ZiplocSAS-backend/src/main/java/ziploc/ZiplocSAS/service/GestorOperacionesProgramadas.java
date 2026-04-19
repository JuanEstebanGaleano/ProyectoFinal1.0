package ziploc.ZiplocSAS.service;

import org.springframework.stereotype.Service;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.structures.ColaPrioridad;

import java.time.LocalDateTime;

@Service
public class GestorOperacionesProgramadas {

    private final ColaPrioridad<OperacionProgramada> cola = new ColaPrioridad<>();
    private final GestorTransacciones gestorTx;

    // Spring inyecta GestorTransacciones automáticamente ────────────────────
    public GestorOperacionesProgramadas(GestorTransacciones gestorTx) {
        this.gestorTx = gestorTx;
    }

    public void programar(String uid, String bOrigen, String bDestino,
                          TipoTransaccion tipo, double monto,
                          LocalDateTime fecha, String desc) {
        OperacionProgramada op = new OperacionProgramada(
                uid, bOrigen, bDestino, tipo, monto, fecha, desc);
        cola.insertar(op);
        System.out.println("📅 Programada: " + op);
    }

    public int ejecutarOperacionesPendientes() {
        int ejecutadas = 0;
        LocalDateTime ahora = LocalDateTime.now();
        ColaPrioridad<OperacionProgramada> pendientes = new ColaPrioridad<>();

        while (!cola.isEmpty()) {
            OperacionProgramada op = cola.extraer();
            if (!op.isEjecutada() && !op.getFechaEjecucion().isAfter(ahora)) {
                ejecutar(op);
                op.marcarEjecutada();
                ejecutadas++;
            } else if (!op.isEjecutada()) {
                pendientes.insertar(op);
            }
        }
        while (!pendientes.isEmpty()) cola.insertar(pendientes.extraer());

        System.out.println("✅ Operaciones ejecutadas: " + ejecutadas);
        return ejecutadas;
    }

    private void ejecutar(OperacionProgramada op) {
        System.out.println("▶️  Ejecutando: " + op.getDescripcion());
        switch (op.getTipo()) {
            case RECARGA ->
                    gestorTx.recargar(op.getUsuarioId(),
                            op.getBilleteraDestinoId(), op.getMonto());
            case RETIRO ->
                    gestorTx.retirar(op.getUsuarioId(),
                            op.getBilleteraOrigenId(), op.getMonto());
            case TRANSFERENCIA_ENVIADA, PAGO_PROGRAMADO ->
                    gestorTx.transferir(op.getUsuarioId(),
                            op.getBilleteraOrigenId(),
                            op.getBilleteraDestinoId(),
                            op.getMonto());
            default ->
                    System.out.println("⚠️ Tipo no ejecutable: " + op.getTipo());
        }
    }

    public int totalPendientes() { return cola.size(); }
}