package ZiplocSAS.structures;

import java.util.*;

/** Grafo dirigido y ponderado para transferencias entre usuarios. */
public class GrafoTransferencias {

    private static class Arista {
        String destino; double monto; int frecuencia;
        Arista(String d, double m) { destino = d; monto = m; frecuencia = 1; }
    }

    private final Map<String, List<Arista>> adj = new HashMap<>();
    private int totalAristas;

    public void agregarVertice(String id) { adj.putIfAbsent(id, new ArrayList<>()); }

    public void agregarTransferencia(String origen, String destino, double monto) {
        agregarVertice(origen); agregarVertice(destino);
        for (Arista a : adj.get(origen)) {
            if (a.destino.equals(destino)) { a.monto += monto; a.frecuencia++; return; }
        }
        adj.get(origen).add(new Arista(destino, monto)); totalAristas++;
    }

    public List<String> bfs(String inicio) {
        List<String> visitados = new ArrayList<>();
        if (!adj.containsKey(inicio)) return visitados;
        Queue<String> cola = new LinkedList<>(); Set<String> visto = new HashSet<>();
        cola.add(inicio); visto.add(inicio);
        while (!cola.isEmpty()) {
            String actual = cola.poll(); visitados.add(actual);
            for (Arista a : adj.getOrDefault(actual, Collections.emptyList()))
                if (!visto.contains(a.destino)) { visto.add(a.destino); cola.add(a.destino); }
        }
        return visitados;
    }

    public boolean tieneCiclo() {
        Set<String> vis = new HashSet<>(), pila = new HashSet<>();
        for (String v : adj.keySet()) if (dfsCiclo(v, vis, pila)) return true;
        return false;
    }

    private boolean dfsCiclo(String n, Set<String> vis, Set<String> pila) {
        if (pila.contains(n)) return true;
        if (vis.contains(n)) return false;
        vis.add(n); pila.add(n);
        for (Arista a : adj.getOrDefault(n, Collections.emptyList()))
            if (dfsCiclo(a.destino, vis, pila)) return true;
        pila.remove(n); return false;
    }

    public Map<String, Integer> getFrecuenciaTransferencias() {
        Map<String, Integer> m = new HashMap<>();
        adj.forEach((k, v) -> { int t = 0; for (Arista a : v) t += a.frecuencia; m.put(k, t); });
        return m;
    }

    public Map<String, Double> getMontosTotales() {
        Map<String, Double> m = new HashMap<>();
        adj.forEach((k, v) -> { double t = 0; for (Arista a : v) t += a.monto; m.put(k, t); });
        return m;
    }

    public List<String> getVecinos(String id) {
        List<String> v = new ArrayList<>();
        adj.getOrDefault(id, Collections.emptyList()).forEach(a -> v.add(a.destino));
        return v;
    }

    public Set<String> getVertices() { return adj.keySet(); }
    public int getTotalVertices() { return adj.size(); }
    public int getTotalAristas() { return totalAristas; }

    public void imprimirGrafo() {
        System.out.println("\n=== GRAFO DE TRANSFERENCIAS ===");
        adj.forEach((k, v) -> {
            System.out.print("  " + k + " -> ");
            v.forEach(a -> System.out.printf("[%s | $%.2f | %dx] ", a.destino, a.monto, a.frecuencia));
            System.out.println();
        });
    }
}