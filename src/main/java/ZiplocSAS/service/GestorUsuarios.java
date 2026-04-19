package ZiplocSAS.service;

import ZiplocSAS.model.*;
import ZiplocSAS.structures.*;
import java.util.List;

public class GestorUsuarios {

    private final TablaHash<String, Usuario> porId = new TablaHash<>();
    private final TablaHash<String, Usuario> porCedula = new TablaHash<>();
    private ArbolBST<Integer, Usuario> arbolPuntos = new ArbolBST<>();

    public boolean registrarUsuario(Usuario u) {
        if (porCedula.containsKey(u.getCedula())) {
            System.out.println("❌ Cédula ya registrada: " + u.getCedula()); return false;
        }
        porId.put(u.getId(), u); porCedula.put(u.getCedula(), u);
        arbolPuntos.insertar(u.getPuntosTotales(), u);
        System.out.println("✅ Registrado: " + u); return true;
    }

    public Usuario buscarPorId(String id) { return porId.get(id); }
    public Usuario buscarPorCedula(String c) { return porCedula.get(c); }

    public boolean actualizarUsuario(String id, String nombre, String email) {
        Usuario u = buscarPorId(id);
        if (u == null) return false;
        arbolPuntos.eliminar(u.getPuntosTotales());
        u.setNombre(nombre); u.setEmail(email);
        arbolPuntos.insertar(u.getPuntosTotales(), u);
        System.out.println("✅ Actualizado: " + u); return true;
    }

    public boolean eliminarUsuario(String id) {
        Usuario u = buscarPorId(id);
        if (u == null) return false;
        arbolPuntos.eliminar(u.getPuntosTotales());
        porCedula.remove(u.getCedula()); porId.remove(id);
        System.out.println("✅ Eliminado: " + id); return true;
    }

    public void actualizarArbolPuntos(Usuario u, int ptsAnteriores) {
        arbolPuntos.eliminar(ptsAnteriores);
        arbolPuntos.insertar(u.getPuntosTotales(), u);
    }

    public List<Usuario> obtenerUsuariosOrdenadosPorPuntos() { return arbolPuntos.inorden(); }
    public List<Usuario> buscarUsuariosPorRangoPuntos(int min, int max) { return arbolPuntos.buscarEnRango(min, max); }
    public List<Usuario> getTodosUsuarios() { return porId.values(); }
    public int totalUsuarios() { return porId.size(); }
}
