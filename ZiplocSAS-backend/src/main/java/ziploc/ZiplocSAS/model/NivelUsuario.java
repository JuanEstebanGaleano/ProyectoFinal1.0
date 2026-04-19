package ziploc.ZiplocSAS.model;

public enum NivelUsuario {
    BRONCE(0, 500), PLATA(501, 1000), ORO(1001, 5000), PLATINO(5001, Integer.MAX_VALUE);

    private final int minPuntos, maxPuntos;

    NivelUsuario(int minPuntos, int maxPuntos) {
        this.minPuntos = minPuntos;
        this.maxPuntos = maxPuntos;
    }

    public static NivelUsuario calcularNivel(int puntos) {
        for (NivelUsuario n : values())
            if (puntos >= n.minPuntos && puntos <= n.maxPuntos) return n;
        return BRONCE;
    }
}