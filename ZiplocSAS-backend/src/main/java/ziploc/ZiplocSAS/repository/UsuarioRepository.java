package ziploc.ZiplocSAS.repository;

import ziploc.ZiplocSAS.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByCedula(String cedula);
    boolean existsByCedula(String cedula);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u ORDER BY u.puntosTotales DESC")
    List<Usuario> findAllOrderByPuntosDesc();

    @Query("SELECT u FROM Usuario u WHERE u.puntosTotales BETWEEN :min AND :max ORDER BY u.puntosTotales DESC")
    List<Usuario> findByRangoPuntos(@Param("min") int min, @Param("max") int max);

    List<Usuario> findByNivel(NivelUsuario nivel);
}