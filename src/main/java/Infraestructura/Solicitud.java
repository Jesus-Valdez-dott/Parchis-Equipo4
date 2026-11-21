package Infraestructura;

/**
 * Petición de un jugador al Host (diagrama 3, pasos 6-8).
 * Lleva identidad (idSolicitud, para deduplicar) y la versión del estado que
 * tenía el jugador cuando la pidió.
 *
 * @author Jesús
 */
public class Solicitud extends MensajeRed {

    private static final long serialVersionUID = 1L;

    private final String idSolicitud;
    private final String tipoAccion;   // LANZAR_DADO o MOVER_FICHA
    private final String idFicha;      // null al lanzar
    private final int version;

    public Solicitud(String idSolicitud, String idJugador, String tipoAccion, String idFicha, int version) {
        super(idJugador, "SOLICITUD");
        this.idSolicitud = idSolicitud;
        this.tipoAccion = tipoAccion;
        this.idFicha = idFicha;
        this.version = version;
    }

    public String getIdSolicitud() { return idSolicitud; }
    public String getIdJugador() { return getIdJugadorEmisor(); }
    public String getTipoAccion() { return tipoAccion; }
    public String getIdFicha() { return idFicha; }
    public int getVersion() { return version; }
}
