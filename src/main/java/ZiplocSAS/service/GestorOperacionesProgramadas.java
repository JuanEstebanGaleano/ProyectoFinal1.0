package ZiplocSAS.service;

import ZiplocSAS.model.*;
import ZiplocSAS.structures.ColaPrioridad;
import java.time.LocalDateTime;

public class GestorOperacionesProgramadas {

    private final ColaPrioridad<OperacionProgramada> cola = new ColaPrioridad<>();
    private final GestorTransacciones gestorTx;

    public GestorOperacionesProgramadas(GestorTransacciones tx) { this.gestorTx = tx; }

    public void programar(String uid, String bOrigen, String bDestino,
                          TipoTransaccion tipo, double monto, LocalDateTime fecha, String desc) {
        OperacionProgramada op = new OperacionProgramada(uid, bOrigen, bDestino, tipo, monto, fecha, desc);
        cola.insertar(op); System.out.println("📅 Programada: " + op);
    }

    public int ejecutarOperacionesPendientes() {
        int ejecutadas = 0; LocalDateTime ahora = LocalDateTime.now();
        ColaPrioridad<OperacionProgramada> pend = new ColaPrioridad<>();
        while (!cola.isEmpty()) {
            OperacionProgramada op = cola.extraer();
            if (!op.isEjecutada() && !op.getFechaEjecucion().isAfter(ahora)) {
                ejecutar(op); op.marcarEjecutada(); ejecutadas++;
            } else if (!op.isEjecutada()) pend.insertar(op);
        }
        while (!pend.isEmpty()) cola.insertar(pend.extraer());
        System.out.println("✅ Operaciones ejecutadas: " + ejecutadas); return ejecutadas;
    }

    private void ejecutar(OperacionProgramada op) {
        switch (op.getTipo()) {
            case RECARGA -> gestorTx.recargar(op.getUsuarioId(), op.getBilleteraDestinoId(), op.getMonto());
            case RETIRO -> gestorTx.retirar(op.getUsuarioId(), op.getBilleteraOrigenId(), op.getMonto());
            default -> gestorTx.transferir(op.getUsuarioId(), op.getBilleteraOrigenId(), op.getBilleteraDestinoId(), op.getMonto());
        }
    }

    public int totalPendientes() { return cola.size(); }
}