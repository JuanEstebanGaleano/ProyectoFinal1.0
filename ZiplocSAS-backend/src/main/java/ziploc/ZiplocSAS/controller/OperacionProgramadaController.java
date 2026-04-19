package ziploc.ZiplocSAS.controller;

import ziploc.ZiplocSAS.dto.request.OperacionProgramadaRequest;
import ziploc.ZiplocSAS.dto.response.ApiResponse;
import ziploc.ZiplocSAS.model.OperacionProgramada;
import ziploc.ZiplocSAS.service.OperacionProgramadaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/operaciones-programadas")
@RequiredArgsConstructor
@Tag(name = "Operaciones Programadas")
public class OperacionProgramadaController {

    private final OperacionProgramadaService opService;

    @PostMapping
    @Operation(summary = "Programar operación (ColaPrioridad)")
    public ResponseEntity<ApiResponse<OperacionProgramada>> programar(
            @RequestBody OperacionProgramadaRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Programada",
                    opService.programar(req.getUsuarioId(), req.getBilleteraOrigenId(),
                            req.getBilleteraDestinoId(), req.getTipo(), req.getMonto(),
                            req.getFechaEjecucion(), req.getDescripcion())));
        } catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PostMapping("/ejecutar")
    @Operation(summary = "Ejecutar pendientes manualmente")
    public ResponseEntity<ApiResponse<Integer>> ejecutar() {
        int n = opService.ejecutarManual();
        return ResponseEntity.ok(ApiResponse.ok("Ejecutadas: " + n, n));
    }

    @GetMapping("/usuario/{uid}")
    public ResponseEntity<ApiResponse<List<OperacionProgramada>>> porUsuario(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.ok("Operaciones", opService.listarPorUsuario(uid)));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<OperacionProgramada>>> pendientes() {
        return ResponseEntity.ok(ApiResponse.ok("Pendientes", opService.listarPendientes()));
    }
}