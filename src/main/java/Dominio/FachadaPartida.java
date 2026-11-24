package Dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * «facade» de la capa de Dominio. En el Host mantiene el estado oficial,
 * valida las acciones que llegan de los jugadores y aplica las reglas.
 *
 * @author Jesús
 */
public class FachadaPartida implements IFachadaPartida {

    private final Juego juego;
    private final ReglasMovimiento reglasMovimiento;
    private final Dado dado;
    private int version;

    public FachadaPartida(Juego juego) {
        this(juego, new Dado());
    }

    /** Permite inyectar un dado (útil para pruebas). */
    public FachadaPartida(Juego juego, Dado dado) {
        this.juego = juego;
        this.reglasMovimiento = new ReglasMovimiento();
        this.dado = dado;
        this.version = 0;
    }

    // ==================================================================
    // Procesamiento de acciones
    // ==================================================================

    @Override
    public synchronized ResultadoAccion procesarAccion(String idJugador, String tipoAccion,
            String idFicha, int versionSolicitud) {

        // Validar y aplicar bajo el mismo bloqueo evita mezclar acciones concurrentes.
        if (versionSolicitud != version) {
            // El cliente debe actualizar su estado antes de intentar otra acción.
            return ResultadoAccion.rechazado("Tu tablero no estaba actualizado; intenta de nuevo", version);
        }
        if (LANZAR_DADO.equals(tipoAccion)) {
            if (!juego.puedeLanzar(idJugador)) {
                return ResultadoAccion.rechazado("No se permite lanzar ahora", version);
            }
            lanzarYRegistrar();
        } else if (MOVER_FICHA.equals(tipoAccion)) {
            if (!juego.puedeMover(idJugador, idFicha)
                    || !reglasMovimiento.esMovimientoValido(juego, idFicha)) {
                return ResultadoAccion.rechazado("La ficha no puede moverse ahora", version);
            }
            moverYAplicarReglas(idJugador, idFicha);
        } else {
            return ResultadoAccion.rechazado("Acción desconocida: " + tipoAccion, version);
        }

        version++;
        return ResultadoAccion.aceptado(version);
    }

    @Override
    public synchronized int obtenerVersion() {
        return version;
    }

    @Override
    public synchronized boolean registrarJugador(Jugador jugador) {
        if (!juego.agregarJugador(jugador)) {
            return false;
        }
        version++;
        return true;
    }

    @Override
    public synchronized boolean verificarDisponibilidad(String idPartida) {
        return juego.getIdPartida().equals(idPartida)
                && "ESPERANDO_JUGADORES".equals(juego.obtenerEstadoJuego())
                && juego.getJugadores().size() < juego.getCapacidadJugadores();
    }

    @Override
    public synchronized boolean registrarJugador(String idJugador) {
        if (idJugador == null || idJugador.isBlank() || !verificarDisponibilidad(juego.getIdPartida())) {
            return false;
        }
        java.util.Set<ColorJugador> ocupados = java.util.EnumSet.noneOf(ColorJugador.class);
        for (Jugador jugador : juego.getJugadores()) {
            ocupados.add(jugador.getColor());
        }
        // Asigna al jugador el primer color disponible en el orden configurado.
        for (ColorJugador color : ColorJugador.values()) {
            if (!ocupados.contains(color)) {
                return registrarJugador(new Jugador(idJugador, idJugador, "avatar1", color));
            }
        }
        return false;
    }

    @Override
    public synchronized boolean iniciarPartida() {
        if (!"ESPERANDO_JUGADORES".equals(juego.obtenerEstadoJuego())
                || juego.getJugadores().size() < 2) {
            return false;
        }
        juego.iniciar();
        version++;
        return true;
    }

    @Override
    public synchronized int obtenerCantidadJugadores() {
        return juego.getJugadores().size();
    }

    @Override
    public synchronized int obtenerCapacidadJugadores() {
        return juego.getCapacidadJugadores();
    }

    // ==================================================================
    // Ejecución de turnos
    // ==================================================================

    /** Lanza el dado y registra el resultado del turno. */
    private void lanzarYRegistrar() {
        Turno turno = juego.getTurno();
        int valor = dado.lanzar();
        turno.registrarTirada(valor);

        if (turno.esTercerSeis()) {
            // El tercer seis cancela el turno y devuelve la última ficha movida.
            String idUltimaFicha = turno.obtenerUltimaFicha();
            if (idUltimaFicha != null) {
                juego.getTablero().regresarUltimaFicha(idUltimaFicha);
            }
            turno.cancelarTurno();
            juego.avanzarTurno();
        } else {
            resolverContinuacion();
        }
    }

    /** Mueve una ficha y aplica las reglas correspondientes. */
    private void moverYAplicarReglas(String idJugador, String idFicha) {
        Turno turno = juego.getTurno();
        Tablero tablero = juego.getTablero();

        int destino = reglasMovimiento.calcularDestino(juego, idFicha);
        boolean saleDeBase = reglasMovimiento.esSalida(juego, idFicha);
        boolean llegaMeta = reglasMovimiento.esLlegadaExacta(juego, idFicha, destino);
        turno.consumirAccion();

        // Captura una ficha rival si corresponde.
        String idVictima = reglasMovimiento.obtenerFichaCapturable(juego, idFicha, destino);
        if (idVictima != null) {
            tablero.capturarFicha(idVictima);
            turno.agregarBonoCaptura(idFicha);
        }

        if (saleDeBase) {
            tablero.sacarFicha(idFicha);
        } else if (llegaMeta) {
            tablero.llevarAMeta(idFicha);
            turno.agregarBonoMeta(idFicha);
        } else {
            tablero.moverFicha(idFicha, destino);     // incluye rebote
        }
        turno.registrarMovimiento(idFicha);

        if (juego.comprobarVictoria(idJugador)) {
            juego.finalizar(idJugador);
        } else {
            resolverContinuacion();
        }
    }

    /** Resuelve las acciones restantes del turno. */
    private void resolverContinuacion() {
        Turno turno = juego.getTurno();

        // Consume acciones sin movimientos legales hasta encontrar una acción jugable.
        while (turno.hayAccionPendiente() && !Turno.ESPERANDO_MOVIMIENTO.equals(turno.getFase())) {
            List<String> opciones = reglasMovimiento.obtenerFichasMovibles(juego);
            juego.guardarOpciones(opciones);
            if (opciones.isEmpty()) {
                turno.consumirAccion(); // Se pierde esta acción si ninguna ficha puede usarla.
            } else {
                turno.esperarMovimiento();
            }
        }

        switch (turno.resolverContinuacion()) {
            case Turno.MOVER:
                break;                          // se espera la selección de ficha
            case Turno.REPETIR:
                juego.guardarOpciones(new ArrayList<>());
                turno.habilitarLanzamiento();   // sacó 6: vuelve a tirar
                break;
            default:
                juego.avanzarTurno();           // CAMBIAR
                break;
        }
    }

    // ==================================================================
    // CONSULTAS (IPartidaLectura)
    // ==================================================================

    @Override
    public synchronized int obtenerValorDado() {
        return juego.obtenerValorDado();
    }

    @Override
    public synchronized String obtenerJugadorActual() {
        return juego.obtenerJugadorActual();
    }

    @Override
    public synchronized List<String> obtenerFichasMovibles() {
        return juego.obtenerFichasMovibles();
    }

    @Override
    public synchronized Map<String, Integer> obtenerPosiciones() {
        return juego.obtenerPosiciones();
    }

    @Override
    public synchronized Map<String, String> obtenerColoresJugadores() {
        return juego.obtenerColoresJugadores();
    }

    @Override
    public synchronized Map<String, String> obtenerNombresJugadores() {
        return juego.obtenerNombresJugadores();
    }

    @Override
    public synchronized Map<String, Integer> obtenerAvataresJugadores() {
        return juego.obtenerAvataresJugadores();
    }

    @Override
    public synchronized String obtenerEstadoJuego() {
        return juego.obtenerEstadoJuego();
    }

    @Override
    public synchronized String obtenerGanador() {
        return juego.getIdGanador();
    }
}
