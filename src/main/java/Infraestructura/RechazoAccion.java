package Infraestructura;

/**
 * Respuesta del Host SOLO al peer que hizo una solicitud inválida
 * (diagrama 4, pasos 26-27). El estado de la partida no cambia.
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
