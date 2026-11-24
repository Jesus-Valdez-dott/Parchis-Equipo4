package Presentacion;

/**
 * Observer del MVC: la vista se registra en el ModeloPresentacion y este le
 * avisa cuando cambia.
 *
 * @author Jesús
 */
public interface ObservadorModelo {

    /** Llegó un estado nuevo de la partida. */
    void notificarCambio();

    /** Hay un aviso para el jugador (acción rechazada); el estado no cambió. */
    void notificarAviso();
}
