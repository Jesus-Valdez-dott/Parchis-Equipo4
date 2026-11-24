package Dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Entidad raíz de la partida.
 *
 * "opciones" son las fichas que el jugador en turno puede mover con la acción
 * actual; las calcula ReglasMovimiento y se guardan con guardarOpciones().
 * obtenerFichasMovibles() las devuelve.
 *
 * @author Jesús
 */
public class Juego {

    private final String idPartida;
    private EstadoJuego estado;
    private final LinkedList<Jugador> ordenTurnos;
    private final List<Jugador> jugadores;
    private final Turno turno;
    private final Tablero tablero;
    private List<String> opciones;
    private int indiceJugadorActual;
    private int numeroSalida;   // número del dado para sacar ficha (configurable, por defecto 5)
    private String idGanador;
    private final int capacidadJugadores;

    public Juego(String idPartida) {
        this(idPartida, 4);
    }

    public Juego(String idPartida, int capacidadJugadores) {
        if (capacidadJugadores < 2 || capacidadJugadores > 4) {
            throw new IllegalArgumentException("La partida debe admitir de 2 a 4 jugadores");
        }
        this.idPartida = idPartida;
        this.capacidadJugadores = capacidadJugadores;
        this.estado = EstadoJuego.CONFIGURACION;
        this.ordenTurnos = new LinkedList<>();
        this.jugadores = new ArrayList<>();
        this.turno = new Turno();
        this.tablero = new Tablero();
        this.opciones = new ArrayList<>();
        this.indiceJugadorActual = 0;
        this.numeroSalida = 5;
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------

    public boolean puedeLanzar(String idJugador) {
        return estado == EstadoJuego.EN_CURSO
                && esJugadorActual(idJugador)
                && Turno.ESPERANDO_LANZAMIENTO.equals(turno.getFase());
    }

    public boolean puedeMover(String idJugador, String idFicha) {
        return estado == EstadoJuego.EN_CURSO
                && esJugadorActual(idJugador)
                && Turno.ESPERANDO_MOVIMIENTO.equals(turno.getFase())
                && opciones.contains(idFicha);
    }

    public void guardarOpciones(List<String> opciones) {
        this.opciones = new ArrayList<>(opciones);
    }

    public boolean comprobarVictoria(String idJugador) {
        Jugador jugador = buscarJugador(idJugador);
        return jugador != null && jugador.tieneTodasLasFichasEnMeta();
    }

    public void finalizar(String idJugador) {
        this.estado = EstadoJuego.FINALIZADO;
        this.idGanador = idJugador;
        this.opciones.clear();
        this.turno.cancelarTurno();
    }

    public void avanzarTurno() {
        if (jugadores.isEmpty()) {
            return;
        }
        turno.cancelarTurno();
        opciones.clear();
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
        turno.habilitarLanzamiento();
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public int obtenerValorDado() {
        return turno.getValorDado();
    }

    public String obtenerJugadorActual() {
        Jugador actual = obtenerJugadorEnTurno();
        return actual != null ? actual.getIdJugador() : null;
    }

    public List<String> obtenerFichasMovibles() {
        return new ArrayList<>(opciones);
    }

    /** idFicha -> posición (ver Ficha para la codificación). */
    public Map<String, Integer> obtenerPosiciones() {
        Map<String, Integer> posiciones = new LinkedHashMap<>();
        for (Jugador jugador : jugadores) {
            for (Ficha ficha : jugador.obtenerFichas()) {
                posiciones.put(ficha.getIdFicha(), ficha.getPosicion());
            }
        }
        return posiciones;
    }

    public Map<String, String> obtenerColoresJugadores() {
        Map<String, String> colores = new LinkedHashMap<>();
        for (Jugador jugador : jugadores) {
            colores.put(jugador.getIdJugador(), jugador.getColor().name());
        }
        return colores;
    }

    public Map<String, String> obtenerNombresJugadores() {
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Jugador jugador : jugadores) {
            nombres.put(jugador.getIdJugador(), jugador.getNombre());
        }
        return nombres;
    }

    /** El avatar se guarda como texto ("avatar1".."avatar12"); aquí se pasa a número 0..11. */
    public Map<String, Integer> obtenerAvataresJugadores() {
        Map<String, Integer> avatares = new LinkedHashMap<>();
        for (Jugador jugador : jugadores) {
            int indice = 0;
            String a = jugador.getAvatar();
            if (a != null && a.matches("avatar\\d+")) {
                indice = Integer.parseInt(a.substring(6)) - 1;
            }
            avatares.put(jugador.getIdJugador(), indice);
        }
        return avatares;
    }

    public String obtenerEstadoJuego() {
        return estado.name();
    }

    /**
     * Tabla de posiciones final (CU Finalizar partida): idJugador -> casillas
     * faltantes, ordenada de menor a mayor (el ganador primero).
     */
    public Map<String, Integer> calcularTablaPosiciones() {
        Map<String, Integer> puntajes = new HashMap<>();
        for (Jugador jugador : jugadores) {
            int faltantes = 0;
            for (Ficha ficha : jugador.obtenerFichas()) {
                faltantes += ficha.calcularCasillasFaltantes();
            }
            puntajes.put(jugador.getIdJugador(), faltantes);
        }
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(puntajes.entrySet());
        Collections.sort(lista, Map.Entry.comparingByValue());
        Map<String, Integer> ordenado = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : lista) {
            ordenado.put(e.getKey(), e.getValue());
        }
        return ordenado;
    }

    // ------------------------------------------------------------------
    // Preparación de la partida (los usan otros CU: unirse / iniciar partida)
    // ------------------------------------------------------------------

    public boolean agregarJugador(Jugador jugador) {
        if (jugador == null || jugadores.size() >= capacidadJugadores
                || (estado != EstadoJuego.CONFIGURACION && estado != EstadoJuego.ESPERANDO_JUGADORES)
                || buscarJugador(jugador.getIdJugador()) != null) {
            return false;
        }
        for (Jugador existente : jugadores) {
            if (existente.getColor() == jugador.getColor()) {
                return false;
            }
        }
        jugadores.add(jugador);
        ordenTurnos.add(jugador);
        tablero.registrarFichas(jugador.obtenerFichas());
        if (estado == EstadoJuego.CONFIGURACION) {
            estado = EstadoJuego.ESPERANDO_JUGADORES;
        }
        return true;
    }

    public void iniciar() {
        if (jugadores.size() >= 2 && estado == EstadoJuego.ESPERANDO_JUGADORES) {
            estado = EstadoJuego.EN_CURSO;
            indiceJugadorActual = 0;
            turno.habilitarLanzamiento();
        }
    }

    public void setNumeroSalida(int numeroSalida) {
        this.numeroSalida = numeroSalida;
    }

    // ------------------------------------------------------------------
    // Auxiliares para ReglasMovimiento y la fachada
    // ------------------------------------------------------------------

    public Jugador obtenerJugadorEnTurno() {
        if (indiceJugadorActual >= 0 && indiceJugadorActual < jugadores.size()) {
            return jugadores.get(indiceJugadorActual);
        }
        return null;
    }

    public Jugador buscarJugador(String idJugador) {
        for (Jugador j : jugadores) {
            if (j.getIdJugador().equals(idJugador)) {
                return j;
            }
        }
        return null;
    }

    private boolean esJugadorActual(String idJugador) {
        String actual = obtenerJugadorActual();
        return actual != null && actual.equals(idJugador);
    }

    public String getIdPartida() { return idPartida; }
    public Tablero getTablero() { return tablero; }
    public Turno getTurno() { return turno; }
    public List<Jugador> getJugadores() { return jugadores; }
    public int getNumeroSalida() { return numeroSalida; }
    public String getIdGanador() { return idGanador; }
    public int getCapacidadJugadores() { return capacidadJugadores; }
}
