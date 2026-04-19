package ziploc.ZiplocSAS.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class RangoFechaRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime desde;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime hasta;
}