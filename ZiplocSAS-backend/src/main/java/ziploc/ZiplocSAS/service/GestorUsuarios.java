package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.structures.*;

import java.util.List;

public class GestorUsuarios {

    private final TablaHash<String, Usuario> porId     = new TablaHash<>();
    private final TablaHash<String, Usuario> porCedula = new TablaHash<>();
    private ArbolBST<Integer, Usuario> arbolPuntos     = new ArbolBST<>();

    // ── Registrar ─────────────────────────────────────────────────────────────
    public boolean registrarUsuario(Usuario u) {
        if (porCedula.containsKey(u.getCedula())) {
            System.out.println("❌ Cédula ya registrada: " + u.getCedula());
            return false;
        }
        porId.put(u.getId(), u);
        porCedula.put(u.getCedula(), u);
        arbolPuntos.insertar(u.getPuntosTotales(), u);
        System.out.println("✅ Registrado: " + u);
        return true;
    }

    // ── Buscar ────────────────────────────────────────────────────────────────
    public Usuario buscarPorId(String id)       { return porId.get(id); }
    public Usuario buscarPorCedula(String c)    { return porCedula.get(c); }

    // ── Actualizar ────────────────────────────────────────────────────────────
    public boolean actualizarUsuario(String id, String nombre, String email) {
        Usuario u = buscarPorId(id);
        if (u == null) { System.out.println("❌ No encontrado: " + id); return false; }
        arbolPuntos.eliminar(u.getPuntosTotales());
        u.setNombre(nombre);
        u.setEmail(email);
        arbolPuntos.insertar(u.getPuntosTotales(), u);
        System.out.println("✅ Actualizado: " + u);
        return true;
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────
    public boolean eliminarUsuario(String id) {
        Usuario u = buscarPorId(id);
        if (u == null) { System.out.println("❌ No encontrado: " + id); return false; }
        arbolPuntos.eliminar(u.getPuntosTotales());
        porCedula.remove(u.getCedula());
        porId.remove(id);
        System.out.println("✅ Eliminado: " + id);
        return true;
    }

    // ── Árbol de puntos ───────────────────────────────────────────────────────
    public void actualizarArbolPuntos(Usuario u, int ptsAnteriores) {
        arbolPuntos.eliminar(ptsAnteriores);
        arbolPuntos.insertar(u.getPuntosTotales(), u);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────
    public List<Usuario> obtenerUsuariosOrdenadosPorPuntos() {
        return arbolPuntos.inorden();
    }

    public List<Usuario> buscarUsuariosPorRangoPuntos(int min, int max) {
        return arbolPuntos.buscarEnRango(min, max);
    }

    public List<Usuario> getTodosUsuarios() { return porId.values(); }
    public int totalUsuarios()              { return porId.size(); }
}