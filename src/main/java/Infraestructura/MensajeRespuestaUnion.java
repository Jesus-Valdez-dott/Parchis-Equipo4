package Infraestructura;

/**
 * Aceptación o rechazo del Host a una solicitud de unión.
 */
public final class MensajeRespuestaUnion extends MensajeRed {

    private static final long serialVersionUID = 1L;

    private final boolean aceptada;
    private final String motivo;

    public MensajeRespuestaUnion(String idJugadorEmisor, String idMensaje,
            boolean aceptada, String motivo) {
        super(idJugadorEmisor, "RESPUESTA_UNION", idMensaje);
        this.aceptada = aceptada;
        this.motivo = motivo;
    }

    public boolean isAceptada() { return aceptada; }
    public String getMotivo() { return motivo; }
}
