package ziploc.ZiplocSAS.repository;
import ziploc.ZiplocSAS.model.Billetera;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BilleteraRepository extends JpaRepository<Billetera, String> {
    List<Billetera> findByUsuarioId(String usuarioId);

    @Query("SELECT b FROM Billetera b ORDER BY b.totalTransacciones DESC")
    List<Billetera> findAllOrderByUsoDesc();
}