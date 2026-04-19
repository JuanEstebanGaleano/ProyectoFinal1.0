package ziploc.ZiplocSAS.structures;

import java.util.ArrayList;
import java.util.List;

/** Árbol Binario de Búsqueda (BST) genérico. */
public class ArbolBST<K extends Comparable<K>, V> {

    private static class Nodo<K, V> {
        K clave; V valor; Nodo<K,V> izq, der;
        Nodo(K c, V v) { clave = c; valor = v; }
    }

    private Nodo<K,V> raiz;
    private int tamanio;

    public void insertar(K clave, V valor) { raiz = insRec(raiz, clave, valor); }

    private Nodo<K,V> insRec(Nodo<K,V> n, K c, V v) {
        if (n == null) { tamanio++; return new Nodo<>(c, v); }
        int cmp = c.compareTo(n.clave);
        if (cmp < 0) n.izq = insRec(n.izq, c, v);
        else if (cmp > 0) n.der = insRec(n.der, c, v);
        else n.valor = v;
        return n;
    }

    public V buscar(K clave) {
        Nodo<K,V> n = raiz;
        while (n != null) {
            int cmp = clave.compareTo(n.clave);
            if (cmp < 0) n = n.izq; else if (cmp > 0) n = n.der; else return n.valor;
        }
        return null;
    }

    public List<V> inorden() { List<V> l = new ArrayList<>(); inRec(raiz, l); return l; }

    private void inRec(Nodo<K,V> n, List<V> l) {
        if (n == null) return; inRec(n.izq, l); l.add(n.valor); inRec(n.der, l);
    }

    public List<V> buscarEnRango(K min, K max) {
        List<V> r = new ArrayList<>(); rangoRec(raiz, min, max, r); return r;
    }

    private void rangoRec(Nodo<K,V> n, K min, K max, List<V> r) {
        if (n == null) return;
        if (n.clave.compareTo(min) > 0) rangoRec(n.izq, min, max, r);
        if (n.clave.compareTo(min) >= 0 && n.clave.compareTo(max) <= 0) r.add(n.valor);
        if (n.clave.compareTo(max) < 0) rangoRec(n.der, min, max, r);
    }

    public void eliminar(K clave) { raiz = elimRec(raiz, clave); }

    private Nodo<K,V> elimRec(Nodo<K,V> n, K c) {
        if (n == null) return null;
        int cmp = c.compareTo(n.clave);
        if (cmp < 0) n.izq = elimRec(n.izq, c);
        else if (cmp > 0) n.der = elimRec(n.der, c);
        else {
            tamanio--;
            if (n.izq == null) return n.der;
            if (n.der == null) return n.izq;
            Nodo<K,V> suc = minNodo(n.der);
            n.clave = suc.clave; n.valor = suc.valor;
            tamanio++; n.der = elimRec(n.der, suc.clave);
        }
        return n;
    }

    private Nodo<K,V> minNodo(Nodo<K,V> n) { while (n.izq != null) n = n.izq; return n; }

    public int size() { return tamanio; }
    public boolean isEmpty() { return raiz == null; }
}