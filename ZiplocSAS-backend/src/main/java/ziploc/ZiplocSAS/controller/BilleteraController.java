package ziploc.ZiplocSAS.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ziploc.ZiplocSAS.dto.request.*;
import ziploc.ZiplocSAS.dto.response.*;
import ziploc.ZiplocSAS.model.Billetera;
import ziploc.ZiplocSAS.service.BilleteraService;
import java.util.List;

@RestController
@RequestMapping("/api/billeteras")
@RequiredArgsConstructor
public class BilleteraController {

    private final BilleteraService billeteraService;

    @PostMapping
    public ResponseEntity<ApiResponse<BilleteraResponse>> crear(
            @Valid @RequestBody CrearBilleteraRequest req) {
        Billetera b = billeteraService.crear(
                req.getUsuarioId(), req.getNombre(), req.getTipo(), req.getSaldoInicial());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Billetera creada", BilleteraResponse.from(b)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BilleteraResponse>> buscar(@PathVariable String id) {
        return billeteraService.buscarPorId(id)
                .map(b -> ResponseEntity.ok(ApiResponse.ok("OK", BilleteraResponse.from(b))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Billetera no encontrada: " + id)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<BilleteraResponse>>> listarPorUsuario(
            @PathVariable String usuarioId) {
        List<BilleteraResponse> lista = billeteraService.listarPorUsuario(usuarioId)
                .stream().map(BilleteraResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("OK", lista));
    }

    @PostMapping("/{id}/recargar")
    public ResponseEntity<ApiResponse<BilleteraResponse>> recargar(
            @PathVariable String id,
            @Valid @RequestBody RecargaRequest req) {
        Billetera b = billeteraService.recargar(id, req.getMonto());
        return ResponseEntity.ok(ApiResponse.ok(
                "Recarga exitosa por $" + req.getMonto(), BilleteraResponse.from(b)));
    }

    @PostMapping("/{id}/retirar")
    public ResponseEntity<ApiResponse<BilleteraResponse>> retirar(
            @PathVariable String id,
            @Valid @RequestBody RetiroRequest req) {
        Billetera b = billeteraService.retirar(id, req.getMonto());
        return ResponseEntity.ok(ApiResponse.ok(
                "Retiro exitoso de $" + req.getMonto(), BilleteraResponse.from(b)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable String id) {
        billeteraService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Billetera desactivada", null));
    }
}