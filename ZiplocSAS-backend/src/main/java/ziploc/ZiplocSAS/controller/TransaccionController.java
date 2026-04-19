package ziploc.ZiplocSAS.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ziploc.ZiplocSAS.dto.request.*;
import ziploc.ZiplocSAS.dto.response.*;
import ziploc.ZiplocSAS.model.Transaccion;
import ziploc.ZiplocSAS.service.TransaccionService;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService txService;

    @PostMapping("/recargar")
    public ResponseEntity<ApiResponse<TransaccionResponse>> recargar(
            @RequestParam String usuarioId,
            @Valid @RequestBody RecargaRequest req) {
        Transaccion tx = txService.recargar(usuarioId, req.getBilleteraId(), req.getMonto());
        return ResponseEntity.ok(ApiResponse.ok("Recarga registrada", TransaccionResponse.from(tx)));
    }

    @PostMapping("/retirar")
    public ResponseEntity<ApiResponse<TransaccionResponse>> retirar(
            @RequestParam String usuarioId,
            @Valid @RequestBody RetiroRequest req) {
        Transaccion tx = txService.retirar(usuarioId, req.getBilleteraId(), req.getMonto());
        return ResponseEntity.ok(ApiResponse.ok("Retiro registrado", TransaccionResponse.from(tx)));
    }

    @PostMapping("/transferir")
    public ResponseEntity<ApiResponse<TransaccionResponse>> transferir(
            @RequestParam String usuarioOrigenId,
            @Valid @RequestBody TransferenciaRequest req) {
        Transaccion tx = txService.transferir(
                usuarioOrigenId,
                req.getBilleteraOrigenId(),
                req.getBilleteraDestinoId(),
                req.getMonto());
        return ResponseEntity.ok(ApiResponse.ok("Transferencia exitosa", TransaccionResponse.from(tx)));
    }

    @PostMapping("/revertir")
    public ResponseEntity<ApiResponse<Void>> revertir(@RequestParam String usuarioId) {
        txService.revertirUltimaOperacion(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok("Operación revertida", null));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<TransaccionResponse>>> historial(
            @PathVariable String usuarioId) {
        List<TransaccionResponse> lista = txService.historialPorUsuario(usuarioId)
                .stream().map(TransaccionResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Historial", lista));
    }

    @GetMapping("/sospechosas")
    public ResponseEntity<ApiResponse<List<TransaccionResponse>>> sospechosas() {
        List<TransaccionResponse> lista = txService.listarSospechosas()
                .stream().map(TransaccionResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Transacciones sospechosas", lista));
    }

    @GetMapping("/grafo")
    public ResponseEntity<ApiResponse<GrafoResponse>> grafo() {
        return ResponseEntity.ok(ApiResponse.ok("Grafo de transferencias", txService.obtenerGrafo()));
    }
}