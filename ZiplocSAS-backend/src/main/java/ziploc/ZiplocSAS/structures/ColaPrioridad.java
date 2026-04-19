package ziploc.ZiplocSAS.structures;

import java.util.NoSuchElementException;

/** Cola de prioridad Min-Heap genérica. */
public class ColaPrioridad<T extends Comparable<T>> {

    private Object[] heap;
    private int tamanio;

    public ColaPrioridad() { heap = new Object[16]; tamanio = 0; }

    public void insertar(T e) {
        if (tamanio >= heap.length) expandir();
        heap[tamanio] = e; subirHeap(tamanio); tamanio++;
    }

    @SuppressWarnings("unchecked")
    public T extraer() {
        if (isEmpty()) throw new NoSuchElementException("Cola vacía");
        T min = (T) heap[0];
        heap[0] = heap[--tamanio]; heap[tamanio] = null;
        if (!isEmpty()) bajarHeap(0);
        return min;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException();
        return (T) heap[0];
    }

    @SuppressWarnings("unchecked")
    private void subirHeap(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (((T)heap[i]).compareTo((T)heap[p]) < 0) { intercambiar(i, p); i = p; }
            else break;
        }
    }

    @SuppressWarnings("unchecked")
    private void bajarHeap(int i) {
        while (true) {
            int m = i, iz = 2*i+1, de = 2*i+2;
            if (iz < tamanio && ((T)heap[iz]).compareTo((T)heap[m]) < 0) m = iz;
            if (de < tamanio && ((T)heap[de]).compareTo((T)heap[m]) < 0) m = de;
            if (m != i) { intercambiar(i, m); i = m; } else break;
        }
    }

    private void intercambiar(int i, int j) { Object t = heap[i]; heap[i] = heap[j]; heap[j] = t; }

    private void expandir() {
        Object[] n = new Object[heap.length * 2];
        System.arraycopy(heap, 0, n, 0, heap.length); heap = n;
    }

    public boolean isEmpty() { return tamanio == 0; }
    public int size() { return tamanio; }
}