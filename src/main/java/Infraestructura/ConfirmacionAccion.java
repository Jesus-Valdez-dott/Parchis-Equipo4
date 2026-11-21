package Infraestructura;

import java.io.Serializable;

/**
 * Actualización que el Host difunde a todos los peers (diagrama 4, pasos 8-9 y 17)
 * con el estado completo de la partida y su versión.
 *
 * El estado viaja como Serializable para que Infraestructura no dependa de las
 * clases de capas superiores; la capa de Aplicación sabe qué hay adentro.
 *
 * @author Jesús
 */
public class ConfirmacionAccion extends MensajeRed {

    private static final long serialVersionUID = 1L;

    private final Serializable estadoCompleto;
    private final int version;

    public ConfirmacionAccion(String idHost, Serializable estadoCompleto, int version) {
        super(idHost, "CONFIRMACION");
        this.estadoCompleto = estadoCompleto;
        this.version = version;
    }

    public Serializable getEstadoCompleto() { return estadoCompleto; }
    public int getVersion() { return version; }
}
