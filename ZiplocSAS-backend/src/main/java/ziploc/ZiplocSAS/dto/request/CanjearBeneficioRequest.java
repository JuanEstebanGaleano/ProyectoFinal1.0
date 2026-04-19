package ziploc.ZiplocSAS.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CanjearBeneficioRequest {

    @NotBlank(message = "El ID de usuario es obligatorio")
    private String usuarioId;

    @NotBlank(message = "El ID de beneficio es obligatorio")
    private String beneficioId;
}