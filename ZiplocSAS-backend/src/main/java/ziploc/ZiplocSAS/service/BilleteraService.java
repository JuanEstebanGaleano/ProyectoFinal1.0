package ziploc.ZiplocSAS.service;

import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.repository.*;
import ziploc.ZiplocSAS.structures.TablaHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BilleteraService {

    private final BilleteraRepository billeteraRepo;
    private final UsuarioRepository usuarioRepo;
    private final TablaHash<String, Billetera> cache = new TablaHash<>();

    @Transactional
    public Billetera crear(String usuarioId, String nombre, TipoBilletera tipo, double saldo) {
        if (!usuarioRepo.existsById(usuarioId))
            throw new IllegalArgumentException("Usuario no encontrado: " + usuarioId);
        Billetera b = new Billetera(nombre, tipo, saldo, usuarioId);
        billeteraRepo.save(b);
        cache.put(b.getId(), b);
        return b;
    }

    public Optional<Billetera> buscarPorId(String id) {
        Billetera cached = cache.get(id);
        if (cached != null) return Optional.of(cached);
        return billeteraRepo.findById(id);
    }

    @Transactional
    public Billetera recargar(String id, double monto) {
        Billetera b = billeteraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Billetera no encontrada: " + id));
        if (!b.isActiva()) throw new IllegalStateException("Billetera inactiva");
        b.recargar(monto);
        b.registrarTransaccion();
        billeteraRepo.save(b);
        cache.put(id, b);
        return b;
    }

    @Transactional
    public Billetera retirar(String id, double monto) {
        Billetera b = billeteraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Billetera no encontrada: " + id));
        if (!b.isActiva()) throw new IllegalStateException("Billetera inactiva");
        if (!b.retirar(monto)) throw new IllegalStateException("Saldo insuficiente: $" + b.getSaldo());
        b.registrarTransaccion();
        billeteraRepo.save(b);
        cache.put(id, b);
        return b;
    }

    @Transactional
    public void desactivar(String id) {
        Billetera b = billeteraRepo.findById(id).orElseThrow();
        b.setActiva(false);
        billeteraRepo.save(b);
        cache.put(id, b);
    }

    public List<Billetera> listarPorUsuario(String uid) { return billeteraRepo.findByUsuarioId(uid); }
    public List<Billetera> listarTodasOrdenadas() { return billeteraRepo.findAllOrderByUsoDesc(); }
    public long total() { return billeteraRepo.count(); }
}