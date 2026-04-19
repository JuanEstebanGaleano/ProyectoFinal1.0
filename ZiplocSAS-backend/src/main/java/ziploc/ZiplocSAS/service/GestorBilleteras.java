package ziploc.ZiplocSAS.service;

import org.springframework.stereotype.Service;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.structures.TablaHash;

@Service
public class GestorBilleteras {

    private final TablaHash<String, Billetera> porId = new TablaHash<>();

    public Billetera crearBilletera(Usuario u, String nombre, TipoBilletera tipo, double saldo) {
        Billetera b = new Billetera(nombre, tipo, saldo, u.getId());
        u.agregarBilletera(b);
        porId.put(b.getId(), b);
        System.out.println("✅ Billetera creada: " + b);
        return b;
    }

    public Billetera buscarPorId(String id) { return porId.get(id); }

    public boolean desactivarBilletera(String id) {
        Billetera b = porId.get(id);
        if (b == null) return false;
        b.setActiva(false);
        System.out.println("⚠️ Desactivada: " + id);
        return true;
    }

    public void listarBilleteras(Usuario u) {
        System.out.println("\n📂 Billeteras de " + u.getNombre() + ":");
        u.getBilleteras().forEach(b -> System.out.println("   " + b));
    }

    public int totalBilleteras() { return porId.size(); }
}