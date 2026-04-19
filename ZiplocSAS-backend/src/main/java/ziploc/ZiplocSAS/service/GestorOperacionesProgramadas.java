package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.structures.ColaPrioridad;

import java.time.LocalDateTime;

public class GestorOperacionesProgramadas {

    private final ColaPrioridad<OperacionProgramada> cola = new ColaPrioridad<>();
    private final GestorTransacciones gestorTx;

    public GestorOperacionesProgramadas(GestorTransacciones tx) {
        this.gestorTx = tx;
    }

    // ── Programar una operación futura ────────────────────────────────────────
    public void programar(String uid, String bOrigen, String bDestino,
                          TipoTransaccion tipo, double monto,
                          LocalDateTime fecha, String desc) {
        OperacionProgramada op = new OperacionProgramada(
                uid, bOrigen, bDestino, tipo, monto, fecha, desc);
        cola.insertar(op);
        System.out.println("📅 Programada: " + op);
    }

    // ── Ejecutar todas las operaciones cuya fecha ya venció ───────────────────
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
                pendientes.insertar(op); // devolver las que aún no vencen
            }
        }

        // Restaurar pendientes no ejecutadas
        while (!pendientes.isEmpty()) cola.insertar(pendientes.extraer());

        System.out.println("✅ Operaciones ejecutadas: " + ejecutadas);
        return ejecutadas;
    }

    // ── Ejecutar una operación individual ────────────────────────────────────
    private void ejecutar(OperacionProgramada op) {
        System.out.println("▶️  Ejecutando: " + op.getDescripcion());
        switch (op.getTipo()) {
            case RECARGA ->
                    gestorTx.recargar(op.getUsuarioId(), op.getBilleteraDestinoId(), op.getMonto());
            case RETIRO ->
                    gestorTx.retirar(op.getUsuarioId(), op.getBilleteraOrigenId(), op.getMonto());
            case TRANSFERENCIA_ENVIADA, PAGO_PROGRAMADO ->
                    gestorTx.transferir(op.getUsuarioId(),
                            op.getBilleteraOrigenId(),
                            op.getBilleteraDestinoId(),
                            op.getMonto());
            default ->
                    System.out.println("⚠️ Tipo no ejecutable: " + op.getTipo());
        }
    }

    // ── Total de operaciones pendientes ───────────────────────────────────────
    public int totalPendientes() { return cola.size(); }
}