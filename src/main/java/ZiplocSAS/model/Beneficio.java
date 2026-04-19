package ZiplocSAS.model;

public class Beneficio {
    public enum TipoBeneficio { REDUCCION_COMISION, BONO_PUNTOS, LIMITE_EXTRA, CASHBACK }

    private final String id, descripcion;
    private final int puntosRequeridos;
    private final TipoBeneficio tipo;

    public Beneficio(String id, String descripcion, int puntosRequeridos, TipoBeneficio tipo) {
        this.id = id; this.descripcion = descripcion;
        this.puntosRequeridos = puntosRequeridos; this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public int getPuntosRequeridos() { return puntosRequeridos; }
    public TipoBeneficio getTipo() { return tipo; }

    @Override
    public String toString() {
        return String.format("Beneficio[%s] %s | %d pts | %s", id, descripcion, puntosRequeridos, tipo);
    }
}