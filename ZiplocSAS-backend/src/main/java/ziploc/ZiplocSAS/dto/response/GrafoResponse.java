package ziploc.ZiplocSAS.dto.response;

import lombok.*;
import java.util.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GrafoResponse {
    private int totalVertices;
    private int totalAristas;
    private boolean tieneCiclos;
    private Map<String, Integer> frecuenciaTransferencias;
    private Map<String, Double> montosTotales;
    private List<String> vertices;
}