package Aplicacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Instantánea (estado completo) de la partida. La arma el GestorJuego del Host
 * con lo que reporta el Dominio, viaja dentro de ConfirmacionAccion y cada peer
 * la entrega a su capa de Presentación. Es inmutable.
 *
 * @author Jesús
 */
public final class EstadoPartidaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int valorDado;
    private final String jugadorActual;
    private final List<String> fichasMovibles;
    private final Map<String, Integer> posiciones;
    private final Map<String, String> coloresJugadores;
    private final String estadoJuego;
    private final String ganador;

    public EstadoPartidaDTO(int valorDado, String jugadorActual, List<String> fichasMovibles,
            Map<String, Integer> posiciones, Map<String, String> coloresJugadores,
            String estadoJuego, String ganador) {
        this.valorDado = valorDado;
        this.jugadorActual = jugadorActual;
        this.fichasMovibles = Collections.unmodifiableList(new ArrayList<>(fichasMovibles));
        this.posiciones = Collections.unmodifiableMap(new LinkedHashMap<>(posiciones));
        this.coloresJugadores = Collections.unmodifiableMap(new LinkedHashMap<>(coloresJugadores));
        this.estadoJuego = estadoJuego;
        this.ganador = ganador;
    }

    public int getValorDado() { return valorDado; }
    public String getJugadorActual() { return jugadorActual; }
    public List<String> getFichasMovibles() { return fichasMovibles; }
    public Map<String, Integer> getPosiciones() { return posiciones; }
    public Map<String, String> getColoresJugadores() { return coloresJugadores; }
    public String getEstadoJuego() { return estadoJuego; }
    public String getGanador() { return ganador; }
}
