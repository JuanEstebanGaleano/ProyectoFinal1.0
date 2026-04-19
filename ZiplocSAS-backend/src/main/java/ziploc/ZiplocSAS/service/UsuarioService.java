package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.*;
import ziploc.ZiplocSAS.structures.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final NotificacionRepository notifRepo;

    // ── Estructuras de datos en memoria ──────────────────────────────────
    private final TablaHash<String, Usuario> cache = new TablaHash<>();
    private final ArbolBST<Integer, String> arbolPuntos = new ArbolBST<>();

    @Transactional
    public Usuario registrar(String nombre, String email, String cedula) {
        if (usuarioRepo.existsByCedula(cedula))
            throw new IllegalArgumentException("Ya existe un usuario con cédula: " + cedula);
        if (usuarioRepo.existsByEmail(email))
            throw new IllegalArgumentException("Ya existe un usuario con email: " + email);
        Usuario u = new Usuario(nombre, email, cedula);
        usuarioRepo.save(u);
        cache.put(u.getId(), u);
        arbolPuntos.insertar(u.getPuntosTotales(), u.getId());
        return u;
    }

    public Optional<Usuario> buscarPorId(String id) {
        Usuario cached = cache.get(id);
        if (cached != null) return Optional.of(cached);
        return usuarioRepo.findById(id);
    }

    public Optional<Usuario> buscarPorCedula(String cedula) {
        return usuarioRepo.findByCedula(cedula);
    }

    @Transactional
    public Usuario actualizar(String id, String nombre, String email) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No encontrado: " + id));
        u.setNombre(nombre);
        u.setEmail(email);
        usuarioRepo.save(u);
        cache.put(id, u);
        return u;
    }

    @Transactional
    public void eliminar(String id) {
        if (!usuarioRepo.existsById(id))
            throw new IllegalArgumentException("No encontrado: " + id);
        usuarioRepo.deleteById(id);
        cache.remove(id);
    }

    @Transactional
    public void sumarPuntos(String id, int puntos) {
        Usuario u = usuarioRepo.findById(id).orElseThrow();
        NivelUsuario nivelAntes = u.getNivel();
        arbolPuntos.eliminar(u.getPuntosTotales());
        u.sumarPuntos(puntos);
        usuarioRepo.save(u);
        cache.put(id, u);
        arbolPuntos.insertar(u.getPuntosTotales(), u.getId());
        if (!u.getNivel().equals(nivelAntes))
            notificar(id, "¡Subiste al nivel " + u.getNivel() + "! 🎉");
    }

    @Transactional
    public void descontarPuntos(String id, int puntos) {
        Usuario u = usuarioRepo.findById(id).orElseThrow();
        arbolPuntos.eliminar(u.getPuntosTotales());
        u.descontarPuntos(puntos);
        usuarioRepo.save(u);
        cache.put(id, u);
        arbolPuntos.insertar(u.getPuntosTotales(), u.getId());
    }

    public void notificar(String usuarioId, String mensaje) {
        notifRepo.save(new Notificacion(usuarioId, mensaje));
    }

    public List<Usuario> listarTodos() { return usuarioRepo.findAll(); }
    public List<Usuario> listarOrdenadosPorPuntos() { return usuarioRepo.findAllOrderByPuntosDesc(); }
    public List<Usuario> buscarPorRangoPuntos(int min, int max) { return usuarioRepo.findByRangoPuntos(min, max); }
    public List<Usuario> buscarPorNivel(NivelUsuario nivel) { return usuarioRepo.findByNivel(nivel); }
    public long total() { return usuarioRepo.count(); }
}