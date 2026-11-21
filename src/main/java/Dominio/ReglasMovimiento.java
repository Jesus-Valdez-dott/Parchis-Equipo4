package Dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de movimiento. No guarda estado: todo lo consulta del Juego.
 * Los "pasos" de un movimiento son los de la acción actual del turno
 * (la tirada del dado o un bono de 20/10).
 *
 * @author Jesús
 */
public class ReglasMovimiento {

    public ReglasMovimiento() {
    }

    // ------------------------------------------------------------------
    // Diagrama 10 - Barreras y opciones legales
    // ------------------------------------------------------------------

    public List<String> obtenerFichasMovibles(Juego juego) {
        List<String> legales = obtenerMovimientosLegales(juego);
        return filtrarPrioridadBarrera(juego, legales);
    }

    public List<String> obtenerMovimientosLegales(Juego juego) {
        List<String> legales = new ArrayList<>();
        Jugador jugador = juego.obtenerJugadorEnTurno();
        AccionPendiente accion = juego.getTurno().obtenerAccionActual();
        if (jugador == null || accion == null) {
            return legales;
        }
        for (Ficha f : jugador.obtenerFichas()) {
            String id = f.getIdFicha();
            if (accion.getFichaObligatoria() != null && !accion.getFichaObligatoria().equals(id)) {
                continue;
            }
            if (id.equals(accion.getFichaExcluida())) {
                continue;
            }
            if (esMovimientoValido(juego, id)) {
                legales.add(id);
            }
        }
        return legales;
    }

    /** Regla 7: con la tirada del dado, si hay barrera propia movible, hay que romperla. */
    public List<String> filtrarPrioridadBarrera(Juego juego, List<String> legales) {
        AccionPendiente accion = juego.getTurno().obtenerAccionActual();
        if (accion == null || !AccionPendiente.DADO.equals(accion.getTipo())) {
            return legales; // los bonos no obligan a romper barrera
        }
        List<String> enBarrera = juego.getTablero().obtenerFichasEnBarrera(juego.obtenerJugadorActual());
        List<String> obligatorias = new ArrayList<>();
        for (String id : legales) {
            if (enBarrera.contains(id)) {
                obligatorias.add(id);
            }
        }
        return obligatorias.isEmpty() ? legales : obligatorias;
    }

    // ------------------------------------------------------------------
    // Diagramas 05 y 06 - Validar / aplicar movimiento
    // ------------------------------------------------------------------

    public boolean esMovimientoValido(Juego juego, String idFicha) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        int pasos = pasosAccionActual(juego);
        if (ficha == null || pasos <= 0) {
            return false;
        }
        switch (ficha.getEstado()) {
            case "EN_BASE":
                return esSalida(juego, idFicha) && juego.getTablero().validarMovimiento(idFicha, pasos);
            case "ACTIVA":
                return juego.getTablero().validarMovimiento(idFicha, pasos);
            default:
                return false; // EN_META
        }
    }

    /** Regla 3: la ficha está en base y la tirada (no un bono) es el número de salida. */
    public boolean esSalida(Juego juego, String idFicha) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        AccionPendiente accion = juego.getTurno().obtenerAccionActual();
        return ficha != null && accion != null
                && "EN_BASE".equals(ficha.getEstado())
                && AccionPendiente.DADO.equals(accion.getTipo())
                && accion.getPasos() == juego.getNumeroSalida();
    }

    /**
     * Devuelve la NUEVA distancia recorrida de la ficha (0 si sale de base).
     * Regla 8: si se pasa de la meta, rebota.
     */
    public int calcularDestino(Juego juego, String idFicha) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        if (ficha == null || "EN_BASE".equals(ficha.getEstado())) {
            return 0;
        }
        return Tablero.calcularDistanciaDestino(ficha.getDistanciaRecorrida(), pasosAccionActual(juego));
    }

    public boolean esLlegadaExacta(Juego juego, String idFicha, int destino) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        return ficha != null && "ACTIVA".equals(ficha.getEstado())
                && destino == Ficha.DISTANCIA_TOTAL_META;
    }

    // ------------------------------------------------------------------
    // Diagrama 08 - Capturar ficha
    // ------------------------------------------------------------------

    /** Regla 5 y 6: captura si en el destino hay UNA ficha rival y no es casilla segura. */
    public String obtenerFichaCapturable(Juego juego, String idFicha, int destino) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        if (ficha == null) {
            return null;
        }
        Tablero tablero = juego.getTablero();
        int casilla = tablero.calcularCasilla(ficha.getColor(), destino);
        if (casilla < 1 || casilla > Ficha.CASILLAS_TABLERO || tablero.esCasillaSegura(casilla)) {
            return null;
        }
        List<Ficha> ocupantes = tablero.obtenerFichasEnCasilla(casilla);
        if (ocupantes.size() == 1 && ocupantes.get(0).getColor() != ficha.getColor()) {
            return ocupantes.get(0).getIdFicha();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private int pasosAccionActual(Juego juego) {
        AccionPendiente accion = juego.getTurno().obtenerAccionActual();
        return accion != null ? accion.getPasos() : 0;
    }

    private Ficha obtenerFichaDelJugador(Juego juego, String idFicha) {
        Jugador jugador = juego.obtenerJugadorEnTurno();
        if (jugador == null) {
            return null;
        }
        for (Ficha f : jugador.obtenerFichas()) {
            if (f.getIdFicha().equals(idFicha)) {
                return f;
            }
        }
        return null;
    }
}
