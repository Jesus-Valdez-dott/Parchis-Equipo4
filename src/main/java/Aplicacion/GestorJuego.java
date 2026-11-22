package Aplicacion;

import Dominio.IFachadaPartida;
import Dominio.ResultadoAccion;
import Infraestructura.ConfirmacionAccion;
import Infraestructura.IGestorRed;
import Infraestructura.INotificadorRed;
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
 * - Host: tiene la FachadaPartida (estado oficial). Procesa las solicitudes
 *   (las propias y las de los peers), difunde las confirmaciones y responde
 *   los rechazos.
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

    private int versionLocal = -1;
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

    /** Este peer se une a la partida del Host. El Host le manda el estado actual. */
    public void unirseAPartida(String ipHost, int puerto) throws IOException {
        gestorRed.conectarAHost(ipHost, puerto);
    }

    // ==================================================================
    // DIAGRAMA 3 - Petición (desde la Presentación local)
    // ==================================================================

    /** Paso 5: el ControladorJuego pide una acción del jugador local. */
    public void solicitarAccion(TipoAccion tipoAccion, String idFicha) {
        Solicitud solicitud = crearSolicitud(tipoAccion, idFicha); // 6
        if (esHost()) {
            procesarSolicitud(solicitud);    // el Host no se manda mensajes a sí mismo
        } else {
            gestorRed.enviar(solicitud);     // 7 -> 8
        }
    }

    /** Paso 6: crear Solicitud con identidad y versión. */
    private synchronized Solicitud crearSolicitud(TipoAccion tipoAccion, String idFicha) {
        return new Solicitud(UUID.randomUUID().toString(), idJugadorLocal,
                tipoAccion.name(), idFicha, versionLocal);
    }

    // ==================================================================
    // DIAGRAMA 4 - Validación y difusión (en el Host)
    // ==================================================================

    /** Paso 1: llega la solicitud de otro peer. */
    @Override
    public void solicitudRecibida(Solicitud solicitud) {
        if (esHost()) {
            procesarSolicitud(solicitud);
        }
    }

    private synchronized void procesarSolicitud(Solicitud solicitud) {
        // 2: deduplicar idSolicitud (sin reejecutar)
        if (!solicitudesProcesadas.add(solicitud.getIdSolicitud())) {
            return;
        }
        recortarHistorial();

        // 3: procesarAccion
        ResultadoAccion resultado = fachada.procesarAccion(solicitud.getIdJugador(),
                solicitud.getTipoAccion(), solicitud.getIdFicha(), solicitud.getVersion());

        if (resultado.isAceptado()) {
            difundirEstadoOficial();                                   // 7 -> 8, 9, 17
        } else {
            responderRechazo(solicitud, resultado.getMotivo());        // 25 -> 26, 27
        }
    }

    /** Arma la instantánea del estado oficial, la difunde y la aplica en el propio Host. */
    private void difundirEstadoOficial() {
        int version = fachada.obtenerVersion();
        EstadoPartidaDTO instantanea = construirInstantanea();
        gestorRed.difundir(new ConfirmacionAccion(idJugadorLocal, instantanea, version));
        aplicarSiEsMasNueva(instantanea, version);
    }

    private void responderRechazo(Solicitud solicitud, String motivo) {
        if (idJugadorLocal.equals(solicitud.getIdJugador())) {
            if (presentacion != null) {
                presentacion.notificarRechazo(motivo);   // el jugador del Host
            }
        } else {
            gestorRed.responderAlSolicitante(solicitud.getIdSolicitud(),
                    new RechazoAccion(idJugadorLocal, solicitud.getIdSolicitud(), motivo));
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
                fachada.obtenerGanador(),
                fachada.obtenerSolicitanteInicio(),
                fachada.obtenerJugadoresConfirmados());
    }

    private void recortarHistorial() {
        while (solicitudesProcesadas.size() > MAX_SOLICITUDES_RECORDADAS) {
            String masVieja = solicitudesProcesadas.iterator().next();
            solicitudesProcesadas.remove(masVieja);
        }
    }

    // ==================================================================
    // DIAGRAMA 4 - Recepción en cada peer
    // ==================================================================

    /** Pasos 10 / 18. */
    @Override
    public void confirmacionRecibida(ConfirmacionAccion confirmacion) {
        if (esHost()) {
            return; // el Host ya aplicó su propio estado oficial
        }
        if (confirmacion.getEstadoCompleto() instanceof EstadoPartidaDTO instantanea) {
            aplicarSiEsMasNueva(instantanea, confirmacion.getVersion());
        }
    }

    /** alt [versión recibida > versión local] -> 11 actualizarEstado; si no -> 16 ignorar. */
    private synchronized void aplicarSiEsMasNueva(EstadoPartidaDTO instantanea, int version) {
        if (version <= versionLocal) {
            return; // estado duplicado o antiguo
        }
        versionLocal = version;
        if (presentacion != null) {
            presentacion.actualizarEstado(instantanea, version);
        }
    }

    /** Paso 28 -> 29. */
    @Override
    public void rechazoRecibido(RechazoAccion rechazo) {
        if (presentacion != null) {
            presentacion.notificarRechazo(rechazo.getMotivo());
        }
    }

    public synchronized int getVersionLocal() {
        return versionLocal;
    }
}
