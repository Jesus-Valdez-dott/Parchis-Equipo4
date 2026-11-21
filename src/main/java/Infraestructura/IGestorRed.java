package Infraestructura;

import java.io.IOException;

/**
 * Interfaz que ofrece el «coordinator» GestorRed al subsistema del juego
 * (diagrama de componentes P2P).
 *
 * @author Jesús
 */
public interface IGestorRed {

    /** El peer asume el rol de Host y acepta conexiones (estrella, hasta 3 peers más). */
    void iniciarComoHost(int puerto) throws IOException;

    /** El peer se une a la partida conectándose al Host. */
    void conectarAHost(String ip, int puerto) throws IOException;

    /** Peer -> Host (diagrama 3, paso 7). */
    void enviar(MensajeRed mensaje);

    /** Host -> todos los peers (diagrama 4, pasos 8-9 y 17). */
    void difundir(MensajeRed mensaje);

    /** Host -> solo el peer que mandó la solicitud (diagrama 4, pasos 26-27). */
    void responderAlSolicitante(String idSolicitud, MensajeRed mensaje);

    void setNotificador(INotificadorRed notificador);

    void cerrar();
}
