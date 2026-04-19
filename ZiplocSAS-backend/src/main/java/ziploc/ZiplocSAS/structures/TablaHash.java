package ziploc.ZiplocSAS.structures;

import java.util.ArrayList;
import java.util.List;

/** Tabla Hash con encadenamiento (chaining). */
public class TablaHash<K, V> {

    private static class Entrada<K, V> {
        K clave; V valor; Entrada<K, V> siguiente;
        Entrada(K c, V v) { clave = c; valor = v; }
    }

    private Object[] cubetas;
    private int tamanio, capacidad;

    public TablaHash() { capacidad = 64; cubetas = new Object[capacidad]; }

    private int hash(K c) { return Math.abs(c.hashCode()) % capacidad; }

    @SuppressWarnings("unchecked")
    public void put(K clave, V valor) {
        if ((double) tamanio / capacidad >= 0.75) rehash();
        int i = hash(clave);
        Entrada<K,V> e = (Entrada<K,V>) cubetas[i];
        while (e != null) { if (e.clave.equals(clave)) { e.valor = valor; return; } e = e.siguiente; }
        Entrada<K,V> nueva = new Entrada<>(clave, valor);
        nueva.siguiente = (Entrada<K,V>) cubetas[i]; cubetas[i] = nueva; tamanio++;
    }

    @SuppressWarnings("unchecked")
    public V get(K clave) {
        Entrada<K,V> e = (Entrada<K,V>) cubetas[hash(clave)];
        while (e != null) { if (e.clave.equals(clave)) return e.valor; e = e.siguiente; }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean remove(K clave) {
        int i = hash(clave);
        Entrada<K,V> e = (Entrada<K,V>) cubetas[i], ant = null;
        while (e != null) {
            if (e.clave.equals(clave)) {
                if (ant == null) cubetas[i] = e.siguiente; else ant.siguiente = e.siguiente;
                tamanio--; return true;
            }
            ant = e; e = e.siguiente;
        }
        return false;
    }

    public boolean containsKey(K c) { return get(c) != null; }

    @SuppressWarnings("unchecked")
    private void rehash() {
        capacidad *= 2; Object[] n = new Object[capacidad];
        for (Object o : cubetas) {
            Entrada<K,V> e = (Entrada<K,V>) o;
            while (e != null) {
                int i = Math.abs(e.clave.hashCode()) % capacidad;
                Entrada<K,V> sig = e.siguiente; e.siguiente = (Entrada<K,V>) n[i]; n[i] = e; e = sig;
            }
        }
        cubetas = n;
    }

    @SuppressWarnings("unchecked")
    public List<V> values() {
        List<V> l = new ArrayList<>();
        for (Object o : cubetas) { Entrada<K,V> e = (Entrada<K,V>) o; while (e != null) { l.add(e.valor); e = e.siguiente; } }
        return l;
    }

    public int size() { return tamanio; }
    public boolean isEmpty() { return tamanio == 0; }
}