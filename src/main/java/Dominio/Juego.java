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
 * actual; las calcula ReglasMovimiento y se guardan con guardarOpciones()
 * (diagrama 10). obtenerFichasMovibles() las devuelve (diagrama 13).
 *
 * @author Jesús
 */
public class Juego {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 4;

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
    private SolicitudInicio solicitudInicio;   // solo existe mientras se espera el inicio

    public Juego(String idPartida) {
        this.idPartida = idPartida;
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
    // Validaciones (diagramas 02 y 05)
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
        if (ordenTurnos.isEmpty()) {
            return;
        }
        turno.cancelarTurno();
        opciones.clear();
        indiceJugadorActual = (indiceJugadorActual + 1) % ordenTurnos.size();
        turno.habilitarLanzamiento();
    }

    // ------------------------------------------------------------------
    // Consultas (diagrama 13)
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
    // CU Iniciar partida
    // ------------------------------------------------------------------

    /**
     * Inscribe a un jugador (lo llama el CU Unirse a partida).
     * Reglas de Iniciar partida: con 4 inscritos la partida inicia sola; si hay
     * una solicitud de inicio pendiente, sus confirmaciones se reinician porque
     * el nuevo también tiene que estar de acuerdo.
     */
    public void agregarJugador(Jugador jugador) {
        if (estado == EstadoJuego.EN_CURSO || estado == EstadoJuego.FINALIZADO) {
            throw new IllegalStateException("La partida ya inició");
        }
        if (jugadores.size() >= MAX_JUGADORES) {
            throw new IllegalStateException("La partida ya tiene " + MAX_JUGADORES + " jugadores");
        }
        jugadores.add(jugador);
        tablero.registrarFichas(jugador.obtenerFichas());
        if (estado == EstadoJuego.CONFIGURACION) {
            estado = EstadoJuego.ESPERANDO_JUGADORES;
        }
        if (jugadores.size() == MAX_JUGADORES) {
            iniciarPartida();
        } else if (solicitudInicio != null) {
            solicitudInicio.reiniciarConfirmaciones();
        }
    }

    /** @return null si el jugador puede pedir el inicio; si no, el motivo. */
    public String validarSolicitudInicio(String idJugador) {
        if (estado != EstadoJuego.ESPERANDO_JUGADORES) {
            return "La partida ya inició";
        }
        if (buscarJugador(idJugador) == null) {
            return "No estás inscrito en la partida";
        }
        if (jugadores.size() < MIN_JUGADORES) {
            return "Se necesitan al menos " + MIN_JUGADORES + " jugadores para iniciar";
        }
        if (solicitudInicio != null) {
            return "Ya hay una solicitud para iniciar la partida";
        }
        return null;
    }

    public void solicitarInicio(String idJugador) {
        solicitudInicio = new SolicitudInicio(idJugador);
        iniciarSiTodosConfirmaron();
    }

    /** @return null si el jugador puede confirmar el inicio; si no, el motivo. */
    public String validarConfirmacionInicio(String idJugador) {
        if (estado != EstadoJuego.ESPERANDO_JUGADORES) {
            return "La partida ya inició";
        }
        if (buscarJugador(idJugador) == null) {
            return "No estás inscrito en la partida";
        }
        if (solicitudInicio == null) {
            return "Nadie ha pedido iniciar la partida";
        }
        if (solicitudInicio.haConfirmado(idJugador)) {
            return "Ya confirmaste el inicio";
        }
        return null;
    }

    public void confirmarInicio(String idJugador) {
        solicitudInicio.registrarConfirmacion(idJugador);
        iniciarSiTodosConfirmaron();
    }

    private void iniciarSiTodosConfirmaron() {
        if (solicitudInicio != null && solicitudInicio.todosConfirmaron(idsInscritos())) {
            iniciarPartida();
        }
    }

    /** Pasa la partida a EN_CURSO; el primero en tirar es el Host (el primero en entrar). */
    public void iniciarPartida() {
        if (estado != EstadoJuego.ESPERANDO_JUGADORES || jugadores.size() < MIN_JUGADORES) {
            return;
        }
        asignarTurnosPorOrdenDeEntrada();
        estado = EstadoJuego.EN_CURSO;
        solicitudInicio = null;
        indiceJugadorActual = 0;
        turno.habilitarLanzamiento();
    }

    /** Decisión del equipo: los turnos siguen el orden de inscripción (el Host primero). */
    private void asignarTurnosPorOrdenDeEntrada() {
        ordenTurnos.clear();
        ordenTurnos.addAll(jugadores);
    }

    public String obtenerSolicitanteInicio() {
        return solicitudInicio != null ? solicitudInicio.getIdSolicitante() : null;
    }

    /** Ids de quienes ya confirmaron, en el orden en que se inscribieron. */
    public List<String> obtenerJugadoresConfirmados() {
        List<String> confirmados = new ArrayList<>();
        if (solicitudInicio != null) {
            for (String id : idsInscritos()) {
                if (solicitudInicio.haConfirmado(id)) {
                    confirmados.add(id);
                }
            }
        }
        return confirmados;
    }

    private List<String> idsInscritos() {
        List<String> ids = new ArrayList<>();
        for (Jugador j : jugadores) {
            ids.add(j.getIdJugador());
        }
        return ids;
    }

    public void setNumeroSalida(int numeroSalida) {
        this.numeroSalida = numeroSalida;
    }

    // ------------------------------------------------------------------
    // Auxiliares para ReglasMovimiento y la fachada
    // ------------------------------------------------------------------

    /** null mientras la partida no ha iniciado (todavía no hay orden de turnos). */
    public Jugador obtenerJugadorEnTurno() {
        if (indiceJugadorActual >= 0 && indiceJugadorActual < ordenTurnos.size()) {
            return ordenTurnos.get(indiceJugadorActual);
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
    public List<Jugador> getOrdenTurnos() { return ordenTurnos; }
}
