package Infraestructura;

import java.io.Serializable;
import java.util.UUID;

/**
 * «artifact» MensajeRed: lo que viaja por TCP entre peers.
 * Tipos: solicitud (Solicitud), actualización (ConfirmacionAccion) y respuesta (RechazoAccion).
 *
 * @author Jesús
 */
public abstract class MensajeRed implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String idJugadorEmisor;
    private final String tipoMensaje;
    private final String idMensaje;

    protected MensajeRed(String idJugadorEmisor, String tipoMensaje) {
        this(idJugadorEmisor, tipoMensaje, UUID.randomUUID().toString());
    }

    protected MensajeRed(String idJugadorEmisor, String tipoMensaje, String idMensaje) {
        this.idJugadorEmisor = idJugadorEmisor;
        this.tipoMensaje = tipoMensaje;
        this.idMensaje = idMensaje;
    }

    public String getIdJugadorEmisor() { return idJugadorEmisor; }
    public String getTipoMensaje() { return tipoMensaje; }
    public String getIdMensaje() { return idMensaje; }
}
