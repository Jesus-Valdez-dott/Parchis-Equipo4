package Infraestructura;

/**
 * Interfaz que implementa el subsistema del juego (GestorJuego) para que el
 * GestorRed le entregue lo que llega por la red.
 *
 * @author Jesús
 */
public interface INotificadorRed {

    /** Recibe un mensaje deserializado desde el peer remoto. */
    void mensajeRecibido(MensajeRed mensaje);

    /** Informa la aceptación de una nueva conexión TCP. */
    void conexionActualizada();

    /** Notifica una desconexión que ocurrió después de conectar al Host. */
    default void conexionPerdida(String motivo) {
    }
}
