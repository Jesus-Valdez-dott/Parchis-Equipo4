package Dominio;

/**
 * Acción que el jugador en turno tiene pendiente de jugar:
 * la tirada del dado (DADO) o un bono (BONO_CAPTURA = 20, BONO_META = 10).
 *
 * @author Jesús
 */
public class AccionPendiente {

    public static final String DADO = "DADO";
    public static final String BONO_CAPTURA = "BONO_CAPTURA";
    public static final String BONO_META = "BONO_META";

    private String tipo;
    private int pasos;
    private String fichaObligatoria; // si no es null, solo esa ficha puede usar la acción
    private String fichaExcluida;    // si no es null, esa ficha NO puede usar la acción

    public AccionPendiente(String tipo, int pasos, String fichaObligatoria, String fichaExcluida) {
        this.tipo = tipo;
        this.pasos = pasos;
        this.fichaObligatoria = fichaObligatoria;
        this.fichaExcluida = fichaExcluida;
    }

    public String getTipo() { return tipo; }
    public int getPasos() { return pasos; }
    public String getFichaObligatoria() { return fichaObligatoria; }
    public String getFichaExcluida() { return fichaExcluida; }

    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setPasos(int pasos) { this.pasos = pasos; }
    public void setFichaObligatoria(String fichaObligatoria) { this.fichaObligatoria = fichaObligatoria; }
    public void setFichaExcluida(String fichaExcluida) { this.fichaExcluida = fichaExcluida; }
}
