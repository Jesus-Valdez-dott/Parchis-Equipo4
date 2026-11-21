package Presentacion;

/**
 * Observer del MVC: la vista se registra en el ModeloPresentacion y este le
 * avisa cuando cambia (diagrama 4, pasos 13/21 y 31).
 *
 * @author Jesús
 */
public interface ObservadorModelo {

    /** Llegó un estado nuevo de la partida. */
    void notificarCambio();

    /** Hay un aviso para el jugador (acción rechazada); el estado no cambió. */
    void notificarAviso();
}
