package ziploc.ZiplocSAS.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TransferenciaRequest {

    @NotBlank(message = "El ID de billetera origen es obligatorio")
    private String billeteraOrigenId;

    @NotBlank(message = "El ID de billetera destino es obligatorio")
    private String billeteraDestinoId;

    @Positive(message = "El monto debe ser mayor a cero")
    private double monto;
}