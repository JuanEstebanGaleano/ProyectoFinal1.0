package ziploc.ZiplocSAS.config;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;
    private final TransaccionService transaccionService;
    private final OperacionProgramadaService opService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("\n📦 Cargando datos de demostración...");

        // Usuarios
        Usuario u1 = usuarioService.registrar("Ana García", "ana@demo.com", "1001001");
        Usuario u2 = usuarioService.registrar("Carlos López", "carlos@demo.com", "1002002");
        Usuario u3 = usuarioService.registrar("María Torres", "maria@demo.com", "1003003");

        // Billeteras
        Billetera b1 = billeteraService.crear(u1.getId(), "Ahorro Ana",       TipoBilletera.AHORRO,         1000.0);
        Billetera b2 = billeteraService.crear(u1.getId(), "Gastos Diarios",   TipoBilletera.GASTOS_DIARIOS,  500.0);
        Billetera b3 = billeteraService.crear(u2.getId(), "Principal Carlos", TipoBilletera.COMPRAS,        2000.0);
        Billetera b4 = billeteraService.crear(u3.getId(), "Transporte María", TipoBilletera.TRANSPORTE,      300.0);

        // Transacciones demo
        transaccionService.recargar(u1.getId(), b1.getId(), 500.0);
        transaccionService.retirar(u1.getId(), b2.getId(), 100.0);
        transaccionService.transferir(u2.getId(), b3.getId(), b1.getId(), 300.0);
        transaccionService.transferir(u1.getId(), b1.getId(), b4.getId(), 150.0);
        transaccionService.recargar(u3.getId(), b4.getId(), 200.0);

        // Operación programada (ya vencida → se puede ejecutar)
        opService.programar(u1.getId(), null, b2.getId(),
                TipoTransaccion.RECARGA, 100.0,
                LocalDateTime.now().minusMinutes(1),
                "Recarga automática demo");

        System.out.println("✅ Demo cargada.");
        System.out.printf("📌 IDs → Ana: %s | Carlos: %s | María: %s%n",
                u1.getId(), u2.getId(), u3.getId());
    }
}