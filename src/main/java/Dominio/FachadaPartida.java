package Dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * «facade» de la capa de Dominio. En el Host mantiene el estado OFICIAL de la
 * partida: valida las acciones que llegan de los jugadores y aplica las reglas.
 *
 * Diagrama 4 (Validación y difusión): procesarAccion -> validar turno, versión y
 * acción -> lanzar y registrar / mover y aplicar reglas -> versión + 1.
 * Por dentro sigue los diagramas del CU Ejecutar turno (02, 03, 05, 06, 08-12).
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
    // DIAGRAMA 4 - procesarAccion
    // ==================================================================

    @Override
    public synchronized ResultadoAccion procesarAccion(String idJugador, String tipoAccion,
            String idFicha, int versionSolicitud) {

        // 4: validar turno, versión y acción
        if (versionSolicitud != version) {
            return ResultadoAccion.rechazado("Tu tablero no estaba actualizado; intenta de nuevo", version);
        }
        if (LANZAR_DADO.equals(tipoAccion)) {
            if (!juego.puedeLanzar(idJugador)) {
                return ResultadoAccion.rechazado("No se permite lanzar ahora", version);
            }
            lanzarYRegistrar(); // 5
        } else if (MOVER_FICHA.equals(tipoAccion)) {
            if (!juego.puedeMover(idJugador, idFicha)
                    || !reglasMovimiento.esMovimientoValido(juego, idFicha)) {
                return ResultadoAccion.rechazado("La ficha no puede moverse ahora", version);
            }
            moverYAplicarReglas(idJugador, idFicha); // 5
        } else {
            return ResultadoAccion.rechazado("Acción desconocida: " + tipoAccion, version);
        }

        // 6: confirmar estado; versión = versión + 1
        version++;
        return ResultadoAccion.aceptado(version); // 7
    }

    @Override
    public synchronized int obtenerVersion() {
        return version;
    }

    // ==================================================================
    // CU EJECUTAR TURNO (lógica de reglas)
    // ==================================================================

    /** Diagramas 02 (lanzar) y 03 (regla del seis). */
    private void lanzarYRegistrar() {
        Turno turno = juego.getTurno();
        int valor = dado.lanzar();
        turno.registrarTirada(valor);

        if (turno.esTercerSeis()) {
            String idUltimaFicha = turno.obtenerUltimaFicha();
            if (idUltimaFicha != null) {
                juego.getTablero().regresarUltimaFicha(idUltimaFicha);
            }
            turno.cancelarTurno();
            juego.avanzarTurno();
        } else {
            resolverContinuacion(); // 12
        }
    }

    /** Diagrama 06 (aplicar movimiento) con 07, 08 y 09. */
    private void moverYAplicarReglas(String idJugador, String idFicha) {
        Turno turno = juego.getTurno();
        Tablero tablero = juego.getTablero();

        int destino = reglasMovimiento.calcularDestino(juego, idFicha);
        boolean saleDeBase = reglasMovimiento.esSalida(juego, idFicha);
        boolean llegaMeta = reglasMovimiento.esLlegadaExacta(juego, idFicha, destino);
        turno.consumirAccion();

        // 08 - Capturar ficha
        String idVictima = reglasMovimiento.obtenerFichaCapturable(juego, idFicha, destino);
        if (idVictima != null) {
            tablero.capturarFicha(idVictima);
            turno.agregarBonoCaptura(idFicha);
        }

        if (saleDeBase) {
            tablero.sacarFicha(idFicha);              // 07
        } else if (llegaMeta) {
            tablero.llevarAMeta(idFicha);             // 09
            turno.agregarBonoMeta(idFicha);
        } else {
            tablero.moverFicha(idFicha, destino);     // incluye rebote
        }
        turno.registrarMovimiento(idFicha);

        if (juego.comprobarVictoria(idJugador)) {
            juego.finalizar(idJugador);
        } else {
            resolverContinuacion(); // 12
        }
    }

    /** Diagrama 12 - Resolver continuación (con 10 y 11 dentro del loop). */
    private void resolverContinuacion() {
        Turno turno = juego.getTurno();

        while (turno.hayAccionPendiente() && !Turno.ESPERANDO_MOVIMIENTO.equals(turno.getFase())) {
            List<String> opciones = reglasMovimiento.obtenerFichasMovibles(juego); // 10
            juego.guardarOpciones(opciones);
            if (opciones.isEmpty()) {
                turno.consumirAccion();   // 11 - Sin movimientos: se pierde la acción
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
    public synchronized String obtenerEstadoJuego() {
        return juego.obtenerEstadoJuego();
    }

    @Override
    public synchronized String obtenerGanador() {
        return juego.getIdGanador();
    }
}
