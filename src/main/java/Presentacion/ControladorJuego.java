package Presentacion;

import Aplicacion.EstadoPartidaDTO;
import Aplicacion.GestorJuego;
import Aplicacion.IPresentacionJuego;
import Aplicacion.TipoAccion;

/**
 * «controller» del MVC (capa de Presentación).
 * - Hacia abajo: recibe lo que hace el jugador en la vista, lo valida con el
 *   ModeloPresentacion y se lo pide al GestorJuego (diagrama 3).
 * - Hacia arriba: el GestorJuego le entrega estados nuevos y rechazos y él
 *   actualiza el ModeloPresentacion (diagrama 4). Nunca toca la vista.
 *
 * @author Jesús
 */
public class ControladorJuego implements IPresentacionJuego {

    private final ModeloPresentacion modelo;
    private final GestorJuego gestorJuego;
    private NavegadorPantallas navegador;   // CU Iniciar partida (puede ser null en pruebas)

    public ControladorJuego(ModeloPresentacion modelo, GestorJuego gestorJuego) {
        this.modelo = modelo;
        this.gestorJuego = gestorJuego;
        this.gestorJuego.setPresentacion(this);
    }

    /**
     * CU Iniciar partida. Se pasa con setter porque el navegador necesita a
     * las pantallas y las pantallas necesitan a este controlador.
     */
    public void setNavegador(NavegadorPantallas navegador) {
        this.navegador = navegador;
    }

    // ==================================================================
    // DIAGRAMA 3 - Petición
    // ==================================================================

    /** Paso 2. idFicha es null al lanzar el dado. */
    public void solicitarAccion(TipoAccion tipoAccion, String idFicha) {
        String motivo = modelo.validarAccion(tipoAccion, idFicha);   // 3 -> 4
        if (motivo != null) {
            modelo.registrarAviso(motivo);
            return;
        }
        gestorJuego.solicitarAccion(tipoAccion, idFicha);            // 5
    }

    // ==================================================================
    // DIAGRAMA 4 - Respuestas del Host
    // ==================================================================

    /**
     * Pasos 11 -> 12. CU Iniciar partida (SD-03): si la partida acaba de pasar
     * a EN_CURSO, se cambia del Lobby a la pantalla de la partida. También
     * cubre al peer que se conecta cuando la partida ya había iniciado.
     */
    @Override
    public void actualizarEstado(EstadoPartidaDTO instantanea, int version) {
        String estadoAnterior = modelo.getEstadoJuego();
        modelo.reemplazarEstado(instantanea, version);
        boolean acabaDeIniciar = "EN_CURSO".equals(instantanea.getEstadoJuego())
                && !"EN_CURSO".equals(estadoAnterior);
        if (acabaDeIniciar && navegador != null) {
            navegador.mostrarPartida();
        }
    }

    /** Pasos 29 -> 30. */
    @Override
    public void notificarRechazo(String motivo) {
        modelo.registrarAviso(motivo);
    }
}
