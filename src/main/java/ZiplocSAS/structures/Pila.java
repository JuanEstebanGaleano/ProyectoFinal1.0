package ZiplocSAS.structures;


import java.util.NoSuchElementException;

/** Pila (Stack) genérica con lista enlazada. */
public class Pila<T> {

    private static class Nodo<T> {
        T dato; Nodo<T> siguiente;
        Nodo(T dato) { this.dato = dato; }
    }

    private Nodo<T> tope;
    private int tamanio;

    public void push(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = tope; tope = nuevo; tamanio++;
    }

    public T pop() {
        if (isEmpty()) throw new NoSuchElementException("Pila vacía");
        T dato = tope.dato; tope = tope.siguiente; tamanio--; return dato;
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Pila vacía");
        return tope.dato;
    }

    public boolean isEmpty() { return tope == null; }
    public int size() { return tamanio; }
}