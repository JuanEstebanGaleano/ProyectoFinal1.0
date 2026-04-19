package ziploc.ZiplocSAS.repository;

import ziploc.ZiplocSAS.model.Notificacion;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, String> {
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(String usuarioId);
    long countByUsuarioIdAndLeidaFalse(String usuarioId);
}