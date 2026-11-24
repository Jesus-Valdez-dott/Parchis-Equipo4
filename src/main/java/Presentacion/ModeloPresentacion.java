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
    // Validación local de acciones
    // ==================================================================

    /**
     * Validación local rápida, antes de molestar al Host.
     * @return null si la acción se puede pedir; si no, el motivo.
     */
    public synchronized String validarAccion(TipoAccion tipoAccion, String idFicha) {
        // Estas comprobaciones evitan peticiones inválidas, pero no sustituyen la validación del Host.
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
    // Actualización del estado y avisos
    // ==================================================================

    /** Reemplaza la representación local y avisa a la vista. */
    public void reemplazarEstado(EstadoPartidaDTO instantanea, int version) {
        synchronized (this) {
            this.estado = instantanea;
            this.version = version;
            this.aviso = null;
        }
        // Notifica fuera del bloqueo para que la vista no se ejecute dentro de la sección crítica.
        notificarCambio();
    }

    /** Guarda el aviso, conserva el estado y avisa a la vista. */
    public void registrarAviso(String motivo) {
        synchronized (this) {
            this.aviso = motivo;
        }
        notificarAviso();
    }

    /** Devuelve los datos que la vista va a dibujar. */
    public synchronized DatosVista obtenerDatosVista() {
        if (estado == null) {
            return new DatosVista(miIdJugador, false, 0, null, null, null, null,
                    null, null, null, null, aviso, version);
        }
        return new DatosVista(miIdJugador, true, estado.getValorDado(), estado.getJugadorActual(),
                estado.getFichasMovibles(), estado.getPosiciones(), estado.getColoresJugadores(),
                estado.getNombresJugadores(), estado.getAvataresJugadores(),
                estado.getEstadoJuego(), estado.getGanador(), aviso, version);
    }

    public String getMiIdJugador() {
        return miIdJugador;
    }
}
