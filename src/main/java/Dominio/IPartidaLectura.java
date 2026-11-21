package Dominio;

import java.util.List;
import java.util.Map;

/**
 * Consultas de solo lectura sobre la partida. La capa de Aplicación las usa
 * para armar la instantánea (estado completo) que se difunde a los peers.
 *
 * @author Jesús
 */
public interface IPartidaLectura {

    int obtenerValorDado();

    String obtenerJugadorActual();

    List<String> obtenerFichasMovibles();

    /**
     * Posición de cada ficha: idFicha -> posición.
     * -1 = en base, 1..68 = número de casilla del tablero,
     * 69..75 = casilla 1..7 de su pasillo, 76 = meta.
     */
    Map<String, Integer> obtenerPosiciones();

    /** Color de cada jugador: idJugador -> nombre del color (ROJO, AZUL, VERDE, AMARILLO). */
    Map<String, String> obtenerColoresJugadores();

    String obtenerEstadoJuego();

    /** idJugador del ganador, o null si la partida no ha terminado. */
    String obtenerGanador();
}
