package ziploc.ZiplocSAS.service;

import org.springframework.stereotype.Service;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.structures.ListaEnlazada;

@Service
public class GestorRecompensas {

    private final ListaEnlazada<Beneficio> catalogo = new ListaEnlazada<>();
    private final GestorUsuarios gu;

    // Spring inyecta GestorUsuarios automáticamente ──────────────────────────
    public GestorRecompensas(GestorUsuarios gu) {
        this.gu = gu;
        inicializarCatalogo();
    }

    private void inicializarCatalogo() {
        catalogo.agregar(new Beneficio("B001", "Reducción 5% comisión",        200,  Beneficio.TipoBeneficio.REDUCCION_COMISION));
        catalogo.agregar(new Beneficio("B002", "Bono de 50 puntos",            300,  Beneficio.TipoBeneficio.BONO_PUNTOS));
        catalogo.agregar(new Beneficio("B003", "Límite extra de transferencia", 500,  Beneficio.TipoBeneficio.LIMITE_EXTRA));
        catalogo.agregar(new Beneficio("B004", "CashBack 2%",                  1000, Beneficio.TipoBeneficio.CASHBACK));
    }

    public boolean canjearBeneficio(String uid, String bid) {
        Usuario u = gu.buscarPorId(uid);
        if (u == null) return false;

        Beneficio b = null;
        for (Beneficio ben : catalogo)
            if (ben.getId().equals(bid)) { b = ben; break; }

        if (b == null) { System.out.println("❌ Beneficio no encontrado."); return false; }
        if (u.getPuntosTotales() < b.getPuntosRequeridos()) {
            System.out.println("❌ Puntos insuficientes. Tienes " + u.getPuntosTotales());
            return false;
        }

        int pts = u.getPuntosTotales();
        u.descontarPuntos(b.getPuntosRequeridos());
        gu.actualizarArbolPuntos(u, pts);

        if (b.getTipo() == Beneficio.TipoBeneficio.BONO_PUNTOS) {
            int p = u.getPuntosTotales();
            u.sumarPuntos(50);
            gu.actualizarArbolPuntos(u, p);
        }

        u.agregarNotificacion("🎁 Canjeaste: " + b.getDescripcion());
        System.out.println("✅ Beneficio canjeado: " + b);
        return true;
    }

    public void mostrarCatalogo() {
        System.out.println("\n🎁 CATÁLOGO:");
        catalogo.forEach(System.out::println);
    }
}