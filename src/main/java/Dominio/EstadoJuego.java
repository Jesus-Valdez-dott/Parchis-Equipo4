package Dominio;

/**
 * Estados de la partida (el diagrama declara Juego.estado : EstadoJuego).
 *
 * @author Jesús
 */
public enum EstadoJuego {
    CONFIGURACION,
    ESPERANDO_JUGADORES,
    EN_CURSO,
    FINALIZADO
}
