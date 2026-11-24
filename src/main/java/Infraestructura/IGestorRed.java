package Infraestructura;

import java.io.IOException;

/**
 * Interfaz que ofrece el «coordinator» GestorRed al subsistema del juego.
 *
 * @author Jesús
 */
public interface IGestorRed {

    /** El peer asume el rol de Host y acepta conexiones (estrella, hasta 3 peers más). */
    void iniciarComoHost(int puerto) throws IOException;

    /** El peer se une a la partida conectándose al Host. */
    void conectarAHost(String ip, int puerto) throws IOException;

    /** Solicita unirse a una partida identificada por idPartida. */
    void solicitarUnion(String idPartida, String idJugador);

    /** Host -> peer que originó el mensaje de solicitud de unión. */
    void enviarRespuestaUnion(MensajeRespuestaUnion mensaje);

    /** Transmite un evento entre los peers según el rol Host/Cliente. */
    void transmitirEvento(MensajeRed mensaje);

    void setNotificador(INotificadorRed notificador);

    void cerrarComunicacion();
}
