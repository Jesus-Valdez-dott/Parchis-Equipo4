package Aplicacion;

import Dominio.IFachadaPartida;
import Dominio.ResultadoAccion;
import Infraestructura.ConfirmacionAccion;
import Infraestructura.IGestorRed;
import Infraestructura.INotificadorRed;
import Infraestructura.MensajeRed;
import Infraestructura.RechazoAccion;
import Infraestructura.Solicitud;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Capa de Aplicación. Coordina Presentación, Dominio e Infraestructura.
 *
 * Todos los peers tienen un GestorJuego; uno de ellos hace de Host:
 * - Host: tiene la FachadaPartida (estado oficial). Procesa solicitudes de
 *   acciones, difunde confirmaciones y responde los rechazos.
 * - Peer cliente: manda sus solicitudes al Host y aplica las confirmaciones
 *   que recibe solo si traen una versión más nueva que la que tiene.
 *
 * @author Jesús
 */
public class GestorJuego implements INotificadorRed {

    private static final int MAX_SOLICITUDES_RECORDADAS = 1000;

    private final String idJugadorLocal;
    private final IGestorRed gestorRed;
    private final IFachadaPartida fachada;   // solo en el Host (null en los demás peers)
    private IPresentacionJuego presentacion;

    // Permanece en -1 hasta recibir el primer estado oficial.
    private int versionLocal = -1;
    // Evita aplicar dos veces una acción si llega repetida por la red.
    private final Set<String> solicitudesProcesadas = new LinkedHashSet<>();

    /**
     * @param fachada la FachadaPartida si este peer es el Host; null si no lo es
     */
    public GestorJuego(String idJugadorLocal, IGestorRed gestorRed, IFachadaPartida fachada) {
        this.idJugadorLocal = idJugadorLocal;
        this.gestorRed = gestorRed;
        this.fachada = fachada;
        this.gestorRed.setNotificador(this);
    }

    public void setPresentacion(IPresentacionJuego presentacion) {
        this.presentacion = presentacion;
    }

    public boolean esHost() {
        return fachada != null;
    }

    public String getIdJugadorLocal() {
        return idJugadorLocal;
    }

    public IGestorRed getGestorRed() {
        return gestorRed;
    }

    public void publicarEstadoOficial() {
        if (!esHost()) {
            throw new IllegalStateException("Solo el Host puede difundir el estado oficial");
        }
        difundirEstadoOficial();
    }

    // ==================================================================
    // ARRANQUE
    // ==================================================================

    /** Este peer asume el rol de Host y publica el estado inicial (versión 0). */
    public void iniciarComoHost(int puerto) throws IOException {
        if (!esHost()) {
            throw new IllegalStateException("Solo el peer que tiene la partida puede ser Host");
        }
        gestorRed.iniciarComoHost(puerto);
        difundirEstadoOficial();
    }

    /** El Host inicia la partida cuando ya hay al menos dos jugadores registrados. */
    public synchronized boolean iniciarPartida() {
        if (!esHost() || !fachada.iniciarPartida()) {
            return false;
        }
        difundirEstadoOficial();
        return true;
    }

    // ==================================================================
    // Solicitud local de acciones
    // ==================================================================

    /** Procesa una acción solicitada por el jugador local. */
    public void solicitarAccion(TipoAccion tipoAccion, String idFicha) {
        Solicitud solicitud = crearSolicitud(tipoAccion, idFicha);
        if (esHost()) {
            // El Host procesa su acción directamente, sin enviársela por TCP.
            procesarSolicitud(solicitud);
        } else {
            gestorRed.transmitirEvento(solicitud);
        }
    }

    /** Crea una solicitud con la identidad y versión locales. */
    private synchronized Solicitud crearSolicitud(TipoAccion tipoAccion, String idFicha) {
        return new Solicitud(UUID.randomUUID().toString(), idJugadorLocal,
                tipoAccion.name(), idFicha, versionLocal);
    }

    // ==================================================================
    // Validación y difusión de acciones en el Host
    // ==================================================================

    /** Procesa una solicitud recibida de otro peer. */
    public void solicitudRecibida(Solicitud solicitud) {
        if (esHost()) {
            procesarSolicitud(solicitud);
        }
    }

    private synchronized void procesarSolicitud(Solicitud solicitud) {
        if (!solicitudesProcesadas.add(solicitud.getIdSolicitud())) {
            return;
        }
        recortarHistorial();

        ResultadoAccion resultado = fachada.procesarAccion(solicitud.getIdJugador(),
                solicitud.getTipoAccion(), solicitud.getIdFicha(), solicitud.getVersion());

        if (resultado.isAceptado()) {
            difundirEstadoOficial();
        } else {
            responderRechazo(solicitud, resultado.getMotivo());
        }
    }

    /** Arma la instantánea del estado oficial, la difunde y la aplica en el propio Host. */
    private void difundirEstadoOficial() {
        int version = fachada.obtenerVersion();
        EstadoPartidaDTO instantanea = construirInstantanea();
        gestorRed.transmitirEvento(new ConfirmacionAccion(idJugadorLocal, instantanea, version));
        aplicarSiEsMasNueva(instantanea, version);
    }

    private void responderRechazo(Solicitud solicitud, String motivo) {
        if (idJugadorLocal.equals(solicitud.getIdJugador())) {
            if (presentacion != null) {
                presentacion.notificarRechazo(motivo);   // el jugador del Host
            }
        } else {
            gestorRed.transmitirEvento(new RechazoAccion(idJugadorLocal, solicitud.getIdSolicitud(), motivo));
        }
    }

    private EstadoPartidaDTO construirInstantanea() {
        return new EstadoPartidaDTO(
                fachada.obtenerValorDado(),
                fachada.obtenerJugadorActual(),
                fachada.obtenerFichasMovibles(),
                fachada.obtenerPosiciones(),
                fachada.obtenerColoresJugadores(),
                fachada.obtenerNombresJugadores(),
                fachada.obtenerAvataresJugadores(),
                fachada.obtenerEstadoJuego(),
                fachada.obtenerGanador());
    }

    private void recortarHistorial() {
        // El historial debe ser acotado para no crecer durante partidas largas.
        while (solicitudesProcesadas.size() > MAX_SOLICITUDES_RECORDADAS) {
            String masVieja = solicitudesProcesadas.iterator().next();
            solicitudesProcesadas.remove(masVieja);
        }
    }

    // ==================================================================
    // Recepción de actualizaciones en cada peer
    // ==================================================================

    /** Aplica la confirmación recibida si corresponde. */
    public void confirmacionRecibida(ConfirmacionAccion confirmacion) {
        if (esHost()) {
            return; // el Host ya aplicó su propio estado oficial
        }
        if (confirmacion.getEstadoCompleto() instanceof EstadoPartidaDTO instantanea) {
            aplicarSiEsMasNueva(instantanea, confirmacion.getVersion());
        }
    }

    /** Actualiza el estado local solo si la versión recibida es más reciente. */
    private synchronized void aplicarSiEsMasNueva(EstadoPartidaDTO instantanea, int version) {
        if (version <= versionLocal) {
            return; // Ignora estados duplicados o antiguos para evitar retroceder.
        }
        versionLocal = version;
        if (presentacion != null) {
            presentacion.actualizarEstado(instantanea, version);
        }
    }

    /** Procesa un rechazo recibido. */
    public void rechazoRecibido(RechazoAccion rechazo) {
        if (presentacion != null) {
            presentacion.notificarRechazo(rechazo.getMotivo());
        }
    }

    @Override
    public void mensajeRecibido(MensajeRed mensaje) {
        if (mensaje instanceof Solicitud solicitud) {
            solicitudRecibida(solicitud);
        } else if (mensaje instanceof ConfirmacionAccion confirmacion) {
            confirmacionRecibida(confirmacion);
        } else if (mensaje instanceof RechazoAccion rechazo) {
            rechazoRecibido(rechazo);
        }
    }

    @Override
    public void conexionActualizada() {
    }

    @Override
    public void conexionPerdida(String motivo) {
        if (presentacion != null) {
            presentacion.notificarRechazo("Se perdió la conexión con el Host: " + motivo);
        }
    }

    public synchronized int getVersionLocal() {
        return versionLocal;
    }

    public void cerrar() {
        gestorRed.cerrarComunicacion();
    }
}
