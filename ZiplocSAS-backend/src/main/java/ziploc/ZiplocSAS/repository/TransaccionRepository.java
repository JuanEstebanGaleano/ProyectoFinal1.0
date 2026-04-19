package ziploc.ZiplocSAS.repository;

import ziploc.ZiplocSAS.model.Transaccion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, String> {
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(String usuarioId);
    List<Transaccion> findByBilleteraOrigenIdOrBilleteraDestinoId(String origen, String destino);
    List<Transaccion> findBySospechosaTrue();

    @Query("SELECT t FROM Transaccion t ORDER BY t.valor DESC")
    List<Transaccion> findAllOrderByValorDesc();

    @Query("SELECT SUM(t.valor) FROM Transaccion t WHERE t.fecha BETWEEN :desde AND :hasta AND t.estado = 'COMPLETADA'")
    Double sumMontoEnRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT t.tipo, COUNT(t) FROM Transaccion t GROUP BY t.tipo")
    List<Object[]> countByTipo();
}