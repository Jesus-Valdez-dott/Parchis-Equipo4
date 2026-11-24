package Infraestructura;

/**
 * Solicitud de un jugador para entrar a una partida identificada por su ID.
 */
public final class MensajeSolicitudUnion extends MensajeRed {

    private static final long serialVersionUID = 1L;

    private final String idPartida;
    private final String idJugador;

    public MensajeSolicitudUnion(String idPartida, String idJugador) {
        super(idJugador, "SOLICITUD_UNION");
        this.idPartida = idPartida;
        this.idJugador = idJugador;
    }

    public String getIdPartida() { return idPartida; }
    public String getIdJugador() { return idJugador; }
}
