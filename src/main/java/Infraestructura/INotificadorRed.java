package Infraestructura;

/**
 * Interfaz que implementa el subsistema del juego (GestorJuego) para que el
 * GestorRed le entregue lo que llega por la red.
 *
 * @author Jesús
 */
public interface INotificadorRed {

    /** En el Host: llegó la solicitud de un peer (diagrama 4, paso 1). */
    void solicitudRecibida(Solicitud solicitud);

    /** En cada peer: llegó una actualización del Host (diagrama 4, pasos 10 y 18). */
    void confirmacionRecibida(ConfirmacionAccion confirmacion);

    /** En el peer solicitante: el Host rechazó su acción (diagrama 4, paso 28). */
    void rechazoRecibido(RechazoAccion rechazo);
}
