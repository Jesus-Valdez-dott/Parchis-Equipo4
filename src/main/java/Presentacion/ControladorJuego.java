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

    public ControladorJuego(ModeloPresentacion modelo, GestorJuego gestorJuego) {
        this.modelo = modelo;
        this.gestorJuego = gestorJuego;
        this.gestorJuego.setPresentacion(this);
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

    /** Pasos 11 -> 12. */
    @Override
    public void actualizarEstado(EstadoPartidaDTO instantanea, int version) {
        modelo.reemplazarEstado(instantanea, version);
    }

    /** Pasos 29 -> 30. */
    @Override
    public void notificarRechazo(String motivo) {
        modelo.registrarAviso(motivo);
    }
}
