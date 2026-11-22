package Presentacion;

import Aplicacion.EstadoPartidaDTO;
import Aplicacion.TipoAccion;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * «model» del MVC (capa de Presentación). Guarda la representación LOCAL del
 * estado de la partida (la última instantánea que confirmó el Host) y los
 * avisos para el jugador. Es el sujeto del patrón Observer.
 *
 * @author Jesús
 */
public class ModeloPresentacion {

    private final String miIdJugador;
    private EstadoPartidaDTO estado;   // null hasta que llega el primer estado del Host
    private int version = -1;
    private String aviso;
    private final List<ObservadorModelo> observadores = new CopyOnWriteArrayList<>();

    public ModeloPresentacion(String miIdJugador) {
        this.miIdJugador = miIdJugador;
    }

    // ==================================================================
    // OBSERVER
    // ==================================================================

    public void agregarObservador(ObservadorModelo observador) {
        observadores.add(observador);
    }

    public void eliminarObservador(ObservadorModelo observador) {
        observadores.remove(observador);
    }

    private void notificarCambio() {
        for (ObservadorModelo o : observadores) {
            o.notificarCambio();
        }
    }

    private void notificarAviso() {
        for (ObservadorModelo o : observadores) {
            o.notificarAviso();
        }
    }

    // ==================================================================
    // DIAGRAMA 3 - validarAccion (pasos 3-4)
    // ==================================================================

    /**
     * Validación local rápida, antes de molestar al Host.
     * @return null si la acción se puede pedir; si no, el motivo.
     */
    public synchronized String validarAccion(TipoAccion tipoAccion, String idFicha) {
        if (estado == null) {
            return "Esperando el estado de la partida";
        }
        if (tipoAccion == TipoAccion.SOLICITAR_INICIO || tipoAccion == TipoAccion.CONFIRMAR_INICIO) {
            return validarAccionDeInicio(tipoAccion);
        }
        if (!"EN_CURSO".equals(estado.getEstadoJuego())) {
            return "La partida no está en curso";
        }
        if (!miIdJugador.equals(estado.getJugadorActual())) {
            return "No es tu turno";
        }
        if (tipoAccion == TipoAccion.LANZAR_DADO && !estado.getFichasMovibles().isEmpty()) {
            return "Primero elige qué ficha mover";
        }
        if (tipoAccion == TipoAccion.MOVER_FICHA && !estado.getFichasMovibles().contains(idFicha)) {
            return "Esa ficha no se puede mover";
        }
        return null;
    }

    /**
     * CU Iniciar partida: la misma validación que hará el Host, pero con la
     * copia local. Si dos peers compiten, el Host decide (SD-02).
     */
    private String validarAccionDeInicio(TipoAccion tipoAccion) {
        if (!"ESPERANDO_JUGADORES".equals(estado.getEstadoJuego())) {
            return "La partida ya inició";
        }
        if (!estado.getNombresJugadores().containsKey(miIdJugador)) {
            return "No estás inscrito en la partida";
        }
        if (tipoAccion == TipoAccion.SOLICITAR_INICIO) {
            if (estado.getNombresJugadores().size() < 2) {
                return "Se necesitan al menos 2 jugadores para iniciar";
            }
            if (estado.getSolicitanteInicio() != null) {
                return "Ya hay una solicitud para iniciar la partida";
            }
        } else {
            if (estado.getSolicitanteInicio() == null) {
                return "Nadie ha pedido iniciar la partida";
            }
            if (estado.getJugadoresConfirmados().contains(miIdJugador)) {
                return "Ya confirmaste el inicio";
            }
        }
        return null;
    }

    // ==================================================================
    // DIAGRAMA 4 - pasos 12-15 y 30-31
    // ==================================================================

    /** Paso 12: reemplaza la representación local y avisa a la vista (13). */
    public void reemplazarEstado(EstadoPartidaDTO instantanea, int version) {
        synchronized (this) {
            this.estado = instantanea;
            this.version = version;
            this.aviso = null;
        }
        notificarCambio();
    }

    /** Paso 30: guarda el aviso, conserva el estado y avisa a la vista (31). */
    public void registrarAviso(String motivo) {
        synchronized (this) {
            this.aviso = motivo;
        }
        notificarAviso();
    }

    /** Paso 14: la vista pide los datos que va a dibujar. */
    public synchronized DatosVista obtenerDatosVista() {
        if (estado == null) {
            return new DatosVista(miIdJugador, false, 0, null, null, null, null,
                    null, null, null, null, null, null, aviso, version);
        }
        return new DatosVista(miIdJugador, true, estado.getValorDado(), estado.getJugadorActual(),
                estado.getFichasMovibles(), estado.getPosiciones(), estado.getColoresJugadores(),
                estado.getNombresJugadores(), estado.getAvataresJugadores(),
                estado.getEstadoJuego(), estado.getGanador(),
                estado.getSolicitanteInicio(), estado.getJugadoresConfirmados(), aviso, version);
    }

    /** Estado del juego de la última instantánea (null si aún no llega). */
    public synchronized String getEstadoJuego() {
        return estado == null ? null : estado.getEstadoJuego();
    }

    public String getMiIdJugador() {
        return miIdJugador;
    }
}
