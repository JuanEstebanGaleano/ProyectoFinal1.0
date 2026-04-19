package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import ziploc.ZiplocSAS.model.*;
import java.util.Map;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReporteGeneralResponse {
    private long totalUsuarios;
    private long totalBilleteras;
    private long totalTransacciones;
    private String usuarioMasActivoNombre;
    private String billeteraTopNombre;
    private int billeteraTopTxs;
    private Map<TipoTransaccion, Integer> frecuenciaPorTipo;
    private Map<TipoBilletera, Integer> actividadPorCategoria;
    private List<UsuarioResponse> usuariosPorPuntos;
    private double montoUltimas24h;
    private int transaccionesSospechosas;
}