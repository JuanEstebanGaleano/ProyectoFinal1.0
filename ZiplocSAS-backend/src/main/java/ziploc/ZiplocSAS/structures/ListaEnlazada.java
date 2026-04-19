package ziploc.ZiplocSAS.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Lista doblemente enlazada genérica. */
public class ListaEnlazada<T> implements Iterable<T> {

    private static class Nodo<T> {
        T dato; Nodo<T> siguiente, anterior;
        Nodo(T dato) { this.dato = dato; }
    }

    private Nodo<T> cabeza, cola;
    private int tamanio;

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cola == null) { cabeza = cola = nuevo; }
        else { nuevo.anterior = cola; cola.siguiente = nuevo; cola = nuevo; }
        tamanio++;
    }

    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) { cabeza = cola = nuevo; }
        else { nuevo.siguiente = cabeza; cabeza.anterior = nuevo; cabeza = nuevo; }
        tamanio++;
    }

    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) throw new IndexOutOfBoundsException("Índice: " + indice);
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) actual = actual.siguiente;
        return actual.dato;
    }

    public T eliminarUltimo() {
        if (cola == null) throw new NoSuchElementException("Lista vacía");
        T dato = cola.dato;
        if (cabeza == cola) { cabeza = cola = null; }
        else { cola = cola.anterior; cola.siguiente = null; }
        tamanio--;
        return dato;
    }

    public T eliminarPrimero() {
        if (cabeza == null) throw new NoSuchElementException("Lista vacía");
        T dato = cabeza.dato;
        if (cabeza == cola) { cabeza = cola = null; }
        else { cabeza = cabeza.siguiente; cabeza.anterior = null; }
        tamanio--;
        return dato;
    }

    public boolean eliminar(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.dato.equals(dato)) {
                if (actual.anterior != null) actual.anterior.siguiente = actual.siguiente;
                else cabeza = actual.siguiente;
                if (actual.siguiente != null) actual.siguiente.anterior = actual.anterior;
                else cola = actual.anterior;
                tamanio--; return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public int size() { return tamanio; }
    public boolean isEmpty() { return tamanio == 0; }
    public T getPrimero() { return cabeza == null ? null : cabeza.dato; }
    public T getUltimo() { return cola == null ? null : cola.dato; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Nodo<T> actual = cabeza;
            public boolean hasNext() { return actual != null; }
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T dato = actual.dato; actual = actual.siguiente; return dato;
            }
        };
    }
    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(", ");
            actual = actual.siguiente;
        }
        return sb.append("]").toString();
    }
}