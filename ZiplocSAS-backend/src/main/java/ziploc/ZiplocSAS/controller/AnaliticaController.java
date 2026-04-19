package ziploc.ZiplocSAS.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ziploc.ZiplocSAS.analytics.AnaliticaMovimientos;
import ziploc.ZiplocSAS.dto.response.*;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.*;
import ziploc.ZiplocSAS.service.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analitica")
@RequiredArgsConstructor
public class AnaliticaController {

    private final AnaliticaMovimientos analitica;
    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;
    private final TransaccionService txService;
    private final TransaccionRepository txRepo;

    // ── 1. Reporte general completo ───────────────────────────────────────────
    @GetMapping("/reporte")
    public ResponseEntity<ApiResponse<ReporteGeneralResponse>> reporte() {
        Usuario ma   = analitica.usuarioMasActivo();
        Billetera mb = analitica.billeteraConMayorUso();
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);

        ReporteGeneralResponse reporte = ReporteGeneralResponse.builder()
                .totalUsuarios(usuarioService.total())
                .totalBilleteras(billeteraService.total())
                .totalTransacciones(txRepo.count())
                .usuarioMasActivoNombre(ma != null ? ma.getNombre() : "N/A")
                .billeteraTopNombre(mb != null ? mb.getNombre() : "N/A")
                .billeteraTopTxs(mb != null ? mb.getTotalTransacciones() : 0)
                .frecuenciaPorTipo(analitica.frecuenciaPorTipo())
                .actividadPorCategoria(analitica.actividadPorCategoria())
                .usuariosPorPuntos(usuarioService.listarOrdenadosPorPuntos()
                        .stream().map(UsuarioResponse::from).toList())
                .montoUltimas24h(analitica.montoTotalEnRango(hace24h, LocalDateTime.now()))
                .transaccionesSospechosas(txService.listarSospechosas().size())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Reporte generado correctamente", reporte));
    }

    // ── 2. Usuarios nivel alto (ORO / PLATINO) ────────────────────────────────
    @GetMapping("/usuarios-nivel-alto")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> nivelAlto() {
        List<UsuarioResponse> lista = analitica.usuariosNivelAlto()
                .stream().map(UsuarioResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios nivel ORO y PLATINO", lista));
    }

    // ── 3. Monto total en rango de fechas ─────────────────────────────────────
    @GetMapping("/monto-rango")
    public ResponseEntity<ApiResponse<Double>> montoEnRango(
            @RequestParam String desde,
            @RequestParam String hasta) {
        LocalDateTime d = LocalDateTime.parse(desde);
        LocalDateTime h = LocalDateTime.parse(hasta);
        double total = analitica.montoTotalEnRango(d, h);
        return ResponseEntity.ok(ApiResponse.ok(
                "Monto total entre " + desde + " y " + hasta, total));
    }

    // ── 4. Grafo de transferencias ────────────────────────────────────────────
    @GetMapping("/grafo")
    public ResponseEntity<ApiResponse<GrafoResponse>> grafo() {
        return ResponseEntity.ok(ApiResponse.ok("Grafo de transferencias", txService.obtenerGrafo()));
    }

    // ── 5. Transacciones sospechosas ──────────────────────────────────────────
    @GetMapping("/sospechosas")
    public ResponseEntity<ApiResponse<List<TransaccionResponse>>> sospechosas() {
        List<TransaccionResponse> lista = txService.listarSospechosas()
                .stream().map(TransaccionResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Transacciones sospechosas", lista));
    }
}