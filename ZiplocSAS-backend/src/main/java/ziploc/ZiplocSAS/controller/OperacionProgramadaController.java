package ziploc.ZiplocSAS.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ziploc.ZiplocSAS.dto.request.OperacionProgramadaRequest;
import ziploc.ZiplocSAS.dto.response.*;
import ziploc.ZiplocSAS.model.OperacionProgramada;
import ziploc.ZiplocSAS.service.OperacionProgramadaService;
import java.util.List;

@RestController
@RequestMapping("/api/operaciones-programadas")
@RequiredArgsConstructor
public class OperacionProgramadaController {

    private final OperacionProgramadaService opService;

    // ── 1. Programar operación ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<OperacionProgramadaResponse>> programar(
            @Valid @RequestBody OperacionProgramadaRequest req) {
        OperacionProgramada op = opService.programar(
                req.getUsuarioId(),
                req.getBilleteraOrigenId(),
                req.getBilleteraDestinoId(),
                req.getTipo(),
                req.getMonto(),
                req.getFechaEjecucion(),
                req.getDescripcion());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Operación programada exitosamente",
                        OperacionProgramadaResponse.from(op)));
    }

    // ── 2. Ejecutar operaciones pendientes ────────────────────────────────────
    @PostMapping("/ejecutar-pendientes")
    public ResponseEntity<ApiResponse<Integer>> ejecutarPendientes() {
        int ejecutadas = opService.ejecutarOperacionesPendientes();
        return ResponseEntity.ok(ApiResponse.ok(
                ejecutadas + " operación(es) ejecutada(s)", ejecutadas));
    }

    // ── 3. Listar por usuario ─────────────────────────────────────────────────
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<OperacionProgramadaResponse>>> listarPorUsuario(
            @PathVariable String usuarioId) {
        List<OperacionProgramadaResponse> lista = opService.listarPorUsuario(usuarioId)
                .stream().map(OperacionProgramadaResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Operaciones de " + usuarioId, lista));
    }

    // ── 4. Listar todas las pendientes ────────────────────────────────────────
    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<OperacionProgramadaResponse>>> pendientes() {
        List<OperacionProgramadaResponse> lista = opService.listarPendientes()
                .stream().map(OperacionProgramadaResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Operaciones pendientes", lista));
    }

    // ── 5. Total pendientes ───────────────────────────────────────────────────
    @GetMapping("/pendientes/total")
    public ResponseEntity<ApiResponse<Integer>> totalPendientes() {
        int total = opService.totalPendientes();
        return ResponseEntity.ok(ApiResponse.ok("Total pendientes: " + total, total));
    }
}