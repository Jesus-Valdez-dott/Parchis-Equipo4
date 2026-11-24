package Aplicacion;

/**
 * Lo que la capa de Aplicación necesita de la capa de Presentación.
 * La implementa ControladorJuego.
 *
 * Se define AQUÍ (en Aplicación) para que las dependencias entre capas sigan
 * yendo solo hacia abajo: Presentación -> Aplicación.
 *
 * @author Jesús
 */
public interface IPresentacionJuego {

    void actualizarEstado(EstadoPartidaDTO instantanea, int version);

    void notificarRechazo(String motivo);
}
