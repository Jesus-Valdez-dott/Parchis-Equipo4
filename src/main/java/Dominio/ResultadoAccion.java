package Dominio;

/**
 * Resultado de FachadaPartida.procesarAccion (diagrama 4):
 * aceptado(resultado, versión) o rechazado(motivo).
 *
 * @author Jesús
 */
public final class ResultadoAccion {

    private final boolean aceptado;
    private final String motivo;
    private final int version;

    private ResultadoAccion(boolean aceptado, String motivo, int version) {
        this.aceptado = aceptado;
        this.motivo = motivo;
        this.version = version;
    }

    public static ResultadoAccion aceptado(int version) {
        return new ResultadoAccion(true, null, version);
    }

    public static ResultadoAccion rechazado(String motivo, int version) {
        return new ResultadoAccion(false, motivo, version);
    }

    public boolean isAceptado() { return aceptado; }
    public String getMotivo() { return motivo; }
    public int getVersion() { return version; }
}
