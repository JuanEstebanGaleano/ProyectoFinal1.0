package ziploc.ZiplocSAS.controller;

import ziploc.ZiplocSAS.dto.request.CrearBilleteraRequest;
import ziploc.ZiplocSAS.dto.response.ApiResponse;
import ziploc.ZiplocSAS.model.Billetera;
import ziploc.ZiplocSAS.service.BilleteraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billeteras")
@RequiredArgsConstructor
@Tag(name = "Billeteras")
public class BilleteraController {

    private final BilleteraService billeteraService;

    @PostMapping
    @Operation(summary = "Crear billetera")
    public ResponseEntity<ApiResponse<Billetera>> crear(@Valid @RequestBody CrearBilleteraRequest req) {
        try {
            Billetera b = billeteraService.crear(req.getUsuarioId(), req.getNombre(), req.getTipo(), req.getSaldoInicial());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Creada", b));
        } catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar billetera")
    public ResponseEntity<ApiResponse<Billetera>> buscar(@PathVariable String id) {
        return billeteraService.buscarPorId(id)
                .map(b -> ResponseEntity.ok(ApiResponse.ok("Encontrada", b)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("No encontrada")));
    }

    @GetMapping("/usuario/{uid}")
    @Operation(summary = "Billeteras por usuario")
    public ResponseEntity<ApiResponse<List<Billetera>>> porUsuario(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.ok("Billeteras", billeteraService.listarPorUsuario(uid)));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking por uso")
    public ResponseEntity<ApiResponse<List<Billetera>>> ranking() {
        return ResponseEntity.ok(ApiResponse.ok("Ranking", billeteraService.listarTodasOrdenadas()));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar billetera")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable String id) {
        billeteraService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Desactivada", null));
    }
}