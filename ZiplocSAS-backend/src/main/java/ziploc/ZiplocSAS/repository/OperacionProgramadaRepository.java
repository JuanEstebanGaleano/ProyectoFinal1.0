package ziploc.ZiplocSAS.repository;

import ziploc.ZiplocSAS.model.OperacionProgramada;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperacionProgramadaRepository extends JpaRepository<OperacionProgramada, String> {
    List<OperacionProgramada> findByUsuarioId(String usuarioId);
    List<OperacionProgramada> findByEjecutadaFalse();

    @Query("SELECT o FROM OperacionProgramada o WHERE o.ejecutada = false AND o.fechaEjecucion <= :ahora ORDER BY o.fechaEjecucion ASC")
    List<OperacionProgramada> findPendientesAEjecutar(@Param("ahora") LocalDateTime ahora);
}