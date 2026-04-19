package ziploc.ZiplocSAS.config;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;
    private final TransaccionService txService;
    private final OperacionProgramadaService opService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("📦 Cargando datos demo...");
        try {
            Usuario ana    = usuarioService.registrar("Ana García",    "ana@fintech.com",    "1001");
            Usuario carlos = usuarioService.registrar("Carlos López",  "carlos@fintech.com", "1002");
            Usuario maria  = usuarioService.registrar("María Torres",  "maria@fintech.com",  "1003");
            Usuario juan   = usuarioService.registrar("Juan Pérez",    "juan@fintech.com",   "1004");

            Billetera b1 = billeteraService.crear(ana.getId(),    "Ahorro Personal", TipoBilletera.AHORRO,        2000.0);
            Billetera b2 = billeteraService.crear(ana.getId(),    "Gastos Diarios",  TipoBilletera.GASTOS_DIARIOS, 500.0);
            Billetera b3 = billeteraService.crear(carlos.getId(), "Principal",       TipoBilletera.COMPRAS,       3000.0);
            Billetera b4 = billeteraService.crear(maria.getId(),  "Transporte",      TipoBilletera.TRANSPORTE,     400.0);
            Billetera b5 = billeteraService.crear(juan.getId(),   "Inversión",       TipoBilletera.INVERSION,     5000.0);

            txService.recargar(ana.getId(),    b1.getId(), 500.0);
            txService.recargar(carlos.getId(), b3.getId(), 1000.0);
            txService.retirar(ana.getId(),     b2.getId(), 100.0);
            txService.transferir(carlos.getId(), b3.getId(), b1.getId(), 300.0);
            txService.transferir(ana.getId(),    b1.getId(), b4.getId(), 200.0);
            txService.recargar(juan.getId(),   b5.getId(), 2000.0);
            txService.transferir(juan.getId(),   b5.getId(), b3.getId(), 800.0);

            opService.programar(ana.getId(), null, b2.getId(),
                    TipoTransaccion.RECARGA, 150.0,
                    LocalDateTime.now().minusMinutes(2), "Recarga automática semanal");

            log.info("✅ Demo lista | Ana={} | Carlos={} | María={} | Juan={}",
                    ana.getId(), carlos.getId(), maria.getId(), juan.getId());
        } catch (Exception e) {
            log.warn("Demo ya cargada: {}", e.getMessage());
        }
    }
}