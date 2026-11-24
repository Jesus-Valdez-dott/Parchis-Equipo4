package Infraestructura;

/**
 * Respuesta del Host solo al peer que hizo una solicitud inválida. El estado
 * de la partida no cambia.
 *
 * @author Jesús
 */
public class RechazoAccion extends MensajeRed {

    private static final long serialVersionUID = 1L;

    private final String idSolicitud;
    private final String motivo;

    public RechazoAccion(String idHost, String idSolicitud, String motivo) {
        super(idHost, "RECHAZO");
        this.idSolicitud = idSolicitud;
        this.motivo = motivo;
    }

    public String getIdSolicitud() { return idSolicitud; }
    public String getMotivo() { return motivo; }
}
