package ziploc.ZiplocSAS.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import ziploc.ZiplocSAS.model.TipoBilletera;

@Data @NoArgsConstructor @AllArgsConstructor
public class CrearBilleteraRequest {

    @NotBlank(message = "El ID de usuario es obligatorio")
    private String usuarioId;

    @NotBlank(message = "El nombre de la billetera es obligatorio")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de billetera es obligatorio")
    private TipoBilletera tipo;

    @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
    private double saldoInicial = 0.0;
}