package ziploc.ZiplocSAS.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RetiroRequest {

    @NotBlank(message = "El ID de billetera es obligatorio")
    private String billeteraId;

    @Positive(message = "El monto debe ser mayor a cero")
    private double monto;
}