package Infraestructura;

import java.io.Serializable;

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

    protected MensajeRed(String idJugadorEmisor, String tipoMensaje) {
        this.idJugadorEmisor = idJugadorEmisor;
        this.tipoMensaje = tipoMensaje;
    }

    public String getIdJugadorEmisor() { return idJugadorEmisor; }
    public String getTipoMensaje() { return tipoMensaje; }
}
