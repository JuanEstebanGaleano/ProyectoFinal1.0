package ziploc.ZiplocSAS.controller;

import ziploc.ZiplocSAS.dto.response.ApiResponse;
import ziploc.ZiplocSAS.dto.response.ReporteResponse;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.NotificacionRepository;
import ziploc.ZiplocSAS.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/analitica")
@RequiredArgsConstructor
@Tag(name = "Analítica")
public class AnaliticaController {

    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;
    private final TransaccionService txService;
    private final NotificacionRepository notifRepo;

    @GetMapping("/reporte-general")
    @Operation(summary = "Reporte general del sistema")
    public ResponseEntity<ApiResponse<ReporteResponse>> reporteGeneral() {
        List<Billetera> billeteras = billeteraService.listarTodasOrdenadas();
        List<Transaccion> txs = txService.obtenerPorValorDesc();
        List<Object[]> freq = txService.frecuenciaPorTipo();

        Map<String, Integer> freqMap = new LinkedHashMap<>();
        for (Object[] row : freq) freqMap.put(row[0].toString(), ((Long) row[1]).intValue());

        List<Usuario> ranking = usuarioService.listarOrdenadosPorPuntos();
        String topUsuario = ranking.isEmpty() ? "N/A" : ranking.get(0).getNombre() + " (" + ranking.get(0).getPuntosTotales() + " pts)";
        String topBilletera = billeteras.isEmpty() ? "N/A" : billeteras.get(0).getNombre() + " (" + billeteras.get(0).getTotalTransacciones() + " txs)";

        return ResponseEntity.ok(ApiResponse.ok("Reporte", ReporteResponse.builder()
                .totalUsuarios((int) usuarioService.total())
                .totalBilleteras((int) billeteraService.total())
                .totalTransacciones((int) txService.total())
                .montoTotalMovilizado(txs.stream().mapToDouble(Transaccion::getValor).sum())
                .usuarioMasActivo(topUsuario)
                .billeteraConMayorUso(topBilletera)
                .frecuenciaPorTipo(freqMap)
                .grafoCiclico(txService.getGrafo().tieneCiclo())
                .build()));
    }

    @GetMapping("/notificaciones/{uid}")
    public ResponseEntity<ApiResponse<List<Notificacion>>> notificaciones(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.ok("Notifs", notifRepo.findByUsuarioIdOrderByFechaDesc(uid)));
    }

    @GetMapping("/notificaciones/{uid}/no-leidas")
    public ResponseEntity<ApiResponse<Long>> noLeidas(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.ok("No leídas", notifRepo.countByUsuarioIdAndLeidaFalse(uid)));
    }
}