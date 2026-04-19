package ziploc.ZiplocSAS.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import ziploc.ZiplocSAS.model.TipoTransaccion;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class OperacionProgramadaRequest {

    @NotBlank(message = "El ID de usuario es obligatorio")
    private String usuarioId;

    private String billeteraOrigenId;

    @NotBlank(message = "El ID de billetera destino es obligatorio")
    private String billeteraDestinoId;

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TipoTransaccion tipo;

    @Positive(message = "El monto debe ser mayor a cero")
    private double monto;

    @NotNull(message = "La fecha de ejecución es obligatoria")
    @Future(message = "La fecha de ejecución debe ser futura")
    private LocalDateTime fechaEjecucion;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200)
    private String descripcion;
}