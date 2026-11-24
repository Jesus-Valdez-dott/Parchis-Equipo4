package Presentacion;

import Aplicacion.EstadoPartidaDTO;
import Aplicacion.GestorJuego;
import Aplicacion.IPresentacionJuego;
import Aplicacion.TipoAccion;

/**
 * «controller» del MVC (capa de Presentación).
 * - Hacia abajo: recibe lo que hace el jugador en la vista, lo valida con el
 *   ModeloPresentacion y se lo pide al GestorJuego.
 * - Hacia arriba: el GestorJuego le entrega estados nuevos y rechazos y él
 *   actualiza el ModeloPresentacion. Nunca toca la vista.
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
    // Solicitudes de acción
    // ==================================================================

    /** idFicha es null al lanzar el dado. */
    public void solicitarAccion(TipoAccion tipoAccion, String idFicha) {
        // El chequeo local da respuesta inmediata; el Host vuelve a validar antes de cambiar el estado.
        String motivo = modelo.validarAccion(tipoAccion, idFicha);
        if (motivo != null) {
            modelo.registrarAviso(motivo);
            return;
        }
        gestorJuego.solicitarAccion(tipoAccion, idFicha);
    }

    // ==================================================================
    // Actualizaciones recibidas del Host
    // ==================================================================

    /** Actualiza el modelo con el estado recibido. */
    @Override
    public void actualizarEstado(EstadoPartidaDTO instantanea, int version) {
        modelo.reemplazarEstado(instantanea, version);
    }

    /** Registra en el modelo el rechazo recibido. */
    @Override
    public void notificarRechazo(String motivo) {
        modelo.registrarAviso(motivo);
    }
}
