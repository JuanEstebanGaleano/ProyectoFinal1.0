package ziploc.ZiplocSAS.controller;

import ziploc.ZiplocSAS.dto.request.TransaccionRequest;
import ziploc.ZiplocSAS.dto.response.ApiResponse;
import ziploc.ZiplocSAS.model.Transaccion;
import ziploc.ZiplocSAS.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
@Tag(name = "Transacciones")
public class TransaccionController {

    private final TransaccionService txService;

    @PostMapping("/recargar")
    @Operation(summary = "Recargar billetera")
    public ResponseEntity<ApiResponse<Transaccion>> recargar(@Valid @RequestBody TransaccionRequest req) {
        try { return ResponseEntity.ok(ApiResponse.ok("Recarga OK", txService.recargar(req.getUsuarioId(), req.getBilleteraOrigenId(), req.getMonto()))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PostMapping("/retirar")
    @Operation(summary = "Retirar de billetera")
    public ResponseEntity<ApiResponse<Transaccion>> retirar(@Valid @RequestBody TransaccionRequest req) {
        try { return ResponseEntity.ok(ApiResponse.ok("Retiro OK", txService.retirar(req.getUsuarioId(), req.getBilleteraOrigenId(), req.getMonto()))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PostMapping("/transferir")
    @Operation(summary = "Transferir entre billeteras")
    public ResponseEntity<ApiResponse<Transaccion>> transferir(@Valid @RequestBody TransaccionRequest req) {
        try { return ResponseEntity.ok(ApiResponse.ok("Transferencia OK", txService.transferir(req.getUsuarioId(), req.getBilleteraOrigenId(), req.getBilleteraDestinoId(), req.getMonto()))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PostMapping("/revertir/{uid}")
    @Operation(summary = "Revertir última operación (Pila LIFO)")
    public ResponseEntity<ApiResponse<Transaccion>> revertir(@PathVariable String uid) {
        try { return ResponseEntity.ok(ApiResponse.ok("Revertida", txService.revertir(uid))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping("/historial/usuario/{uid}")
    public ResponseEntity<ApiResponse<List<Transaccion>>> historialUsuario(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.ok("Historial", txService.historialPorUsuario(uid)));
    }

    @GetMapping("/historial/billetera/{bid}")
    public ResponseEntity<ApiResponse<List<Transaccion>>> historialBilletera(@PathVariable String bid) {
        return ResponseEntity.ok(ApiResponse.ok("Historial", txService.historialPorBilletera(bid)));
    }

    @GetMapping("/sospechosas")
    public ResponseEntity<ApiResponse<List<Transaccion>>> sospechosas() {
        return ResponseEntity.ok(ApiResponse.ok("Sospechosas", txService.obtenerSospechosas()));
    }

    @GetMapping("/grafo/visualizar")
    @Operation(summary = "Datos del grafo de transferencias")
    public ResponseEntity<ApiResponse<Map<String, Object>>> grafo() {
        var g = txService.getGrafo();
        return ResponseEntity.ok(ApiResponse.ok("Grafo", Map.of(
                "vertices", g.getTotalVertices(),
                "aristas", g.getTotalAristas(),
                "tieneCiclos", g.tieneCiclo(),
                "frecuencias", g.getFrecuenciaTransferencias(),
                "montos", g.getMontosTotales()
        )));
    }

    @GetMapping("/grafo/bfs/{uid}")
    @Operation(summary = "BFS desde usuario (Grafo)")
    public ResponseEntity<ApiResponse<List<String>>> bfs(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.ok("BFS", txService.getGrafo().bfs(uid)));
    }

    @GetMapping("/monto-rango")
    public ResponseEntity<ApiResponse<Double>> montoRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ApiResponse.ok("Monto total", txService.montoEnRango(desde, hasta)));
    }
}