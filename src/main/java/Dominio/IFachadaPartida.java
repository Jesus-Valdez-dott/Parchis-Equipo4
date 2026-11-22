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
    String SOLICITAR_INICIO = "SOLICITAR_INICIO";
    String CONFIRMAR_INICIO = "CONFIRMAR_INICIO";

    /**
     * Diagrama 4, pasos 3-7 / 25: valida turno, versión y acción; si es válida
     * la aplica, incrementa la versión y la regresa como aceptada.
     *
     * @param idJugador        jugador que pide la acción
     * @param tipoAccion       LANZAR_DADO, MOVER_FICHA, SOLICITAR_INICIO o CONFIRMAR_INICIO
     * @param idFicha          ficha a mover (null al lanzar)
     * @param versionSolicitud versión del estado que tenía el jugador al pedirla
     */
    ResultadoAccion procesarAccion(String idJugador, String tipoAccion, String idFicha, int versionSolicitud);

    /** Versión del estado oficial; aumenta en 1 con cada acción aceptada. */
    int obtenerVersion();
}
