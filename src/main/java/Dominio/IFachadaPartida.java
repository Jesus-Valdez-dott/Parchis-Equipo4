package Dominio;

/**
 * Punto de entrada a la capa de Dominio (lo usa la capa de Aplicación).
 * Solo el Host la usa para modificar la partida: es el estado oficial.
 *
 * @author Jesús
 */
public interface IFachadaPartida extends IPartidaLectura {

    String LANZAR_DADO = "LANZAR_DADO";
    String MOVER_FICHA = "MOVER_FICHA";

    /**
     * Valida turno, versión y acción; si es válida, la aplica, incrementa la
     * versión y la regresa como aceptada.
     *
     * @param idJugador        jugador que pide la acción
     * @param tipoAccion       LANZAR_DADO o MOVER_FICHA
     * @param idFicha          ficha a mover (null al lanzar)
     * @param versionSolicitud versión del estado que tenía el jugador al pedirla
     */
    ResultadoAccion procesarAccion(String idJugador, String tipoAccion, String idFicha, int versionSolicitud);

    /** Registra un jugador mientras la partida espera participantes. */
    boolean registrarJugador(Jugador jugador);

    /** Comprueba que una partida identificada por idPartida admita jugadores. */
    boolean verificarDisponibilidad(String idPartida);

    /** Registra al jugador indicado por su identidad recibida en la solicitud. */
    boolean registrarJugador(String idJugador);

    /** Inicia la partida si ya hay al menos dos jugadores registrados. */
    boolean iniciarPartida();

    int obtenerCantidadJugadores();

    int obtenerCapacidadJugadores();

    /** Versión del estado oficial; aumenta en 1 con cada acción aceptada. */
    int obtenerVersion();
}
