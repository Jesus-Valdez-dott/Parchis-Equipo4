package Presentacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lo que la vista necesita para dibujarse (diagrama 4, pasos 14-15:
 * obtenerDatosVista -> datos actualizados). Ya viene "masticado" para la vista.
 *
 * @author Jesús
 */
public final class DatosVista {

    private final String miIdJugador;
    private final boolean hayEstado;
    private final int valorDado;
    private final String jugadorActual;
    private final List<String> fichasMovibles;
    private final Map<String, Integer> posiciones;
    private final Map<String, String> coloresJugadores;
    private final String estadoJuego;
    private final String ganador;
    private final String aviso;
    private final int version;

    DatosVista(String miIdJugador, boolean hayEstado, int valorDado, String jugadorActual,
            List<String> fichasMovibles, Map<String, Integer> posiciones,
            Map<String, String> coloresJugadores, String estadoJuego, String ganador,
            String aviso, int version) {
        this.miIdJugador = miIdJugador;
        this.hayEstado = hayEstado;
        this.valorDado = valorDado;
        this.jugadorActual = jugadorActual;
        this.fichasMovibles = fichasMovibles != null ? fichasMovibles : Collections.emptyList();
        this.posiciones = posiciones != null ? posiciones : new LinkedHashMap<>();
        this.coloresJugadores = coloresJugadores != null ? coloresJugadores : new HashMap<>();
        this.estadoJuego = estadoJuego;
        this.ganador = ganador;
        this.aviso = aviso;
        this.version = version;
    }

    public boolean isEnCurso() {
        return "EN_CURSO".equals(estadoJuego);
    }

    public boolean isMiTurno() {
        return isEnCurso() && miIdJugador.equals(jugadorActual);
    }

    /** En su turno, si no hay fichas por elegir, toca lanzar. */
    public boolean isPuedeLanzar() {
        return isMiTurno() && fichasMovibles.isEmpty();
    }

    /** Fichas que el jugador local puede elegir ahora mismo. */
    public List<String> getFichasSeleccionables() {
        return isMiTurno() ? fichasMovibles : new ArrayList<>();
    }

    public String getMiIdJugador() { return miIdJugador; }
    public boolean isHayEstado() { return hayEstado; }
    public int getValorDado() { return valorDado; }
    public String getJugadorActual() { return jugadorActual; }
    public List<String> getFichasMovibles() { return fichasMovibles; }
    public Map<String, Integer> getPosiciones() { return posiciones; }
    public Map<String, String> getColoresJugadores() { return coloresJugadores; }
    public String getEstadoJuego() { return estadoJuego; }
    public String getGanador() { return ganador; }
    public String getAviso() { return aviso; }
    public int getVersion() { return version; }
}
