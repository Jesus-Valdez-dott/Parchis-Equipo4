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
                    null, null, aviso, version);
        }
        return new DatosVista(miIdJugador, true, estado.getValorDado(), estado.getJugadorActual(),
                estado.getFichasMovibles(), estado.getPosiciones(), estado.getColoresJugadores(),
                estado.getEstadoJuego(), estado.getGanador(), aviso, version);
    }

    public String getMiIdJugador() {
        return miIdJugador;
    }
}
