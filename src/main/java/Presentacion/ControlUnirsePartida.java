package Presentacion;

import Dominio.IFachadaPartida;
import Infraestructura.IGestorRed;
import Infraestructura.INotificadorRed;
import Infraestructura.MensajeRed;
import Infraestructura.MensajeRespuestaUnion;
import Infraestructura.MensajeSolicitudUnion;
import java.io.IOException;

/**
 * Coordina la conexión y el registro en la sala. El Host valida la solicitud;
 * el cliente muestra el resultado y entrega al juego los mensajes posteriores.
 */
public final class ControlUnirsePartida implements INotificadorRed {

    private final IGestorRed gestorRed;
    private final IFachadaPartida fachada;
    private final PantallaUnirsePartida pantalla;
    private final INotificadorRed notificadorJuego;
    private final Runnable alRegistrarJugador;
    private final String idPartida;
    private final String idJugador;

    public ControlUnirsePartida(IGestorRed gestorRed, IFachadaPartida fachada,
            PantallaUnirsePartida pantalla, INotificadorRed notificadorJuego,
            Runnable alRegistrarJugador, String idPartida, String idJugador) {
        this.gestorRed = gestorRed;
        this.fachada = fachada;
        this.pantalla = pantalla;
        this.notificadorJuego = notificadorJuego;
        this.alRegistrarJugador = alRegistrarJugador;
        this.idPartida = idPartida;
        this.idJugador = idJugador;
        this.gestorRed.setNotificador(this);
    }

    public void unirsePartida(String ip, int puerto) throws IOException {
        // La solicitud se envía solo después de abrir la conexión TCP.
        gestorRed.conectarAHost(ip, puerto);
        gestorRed.solicitarUnion(idPartida, idJugador);
    }

    public void procesarSolicitudUnion(MensajeSolicitudUnion mensaje) {
        if (fachada == null) {
            throw new IllegalStateException("Solo el Host puede procesar solicitudes de unión");
        }
        boolean aceptada = fachada.verificarDisponibilidad(mensaje.getIdPartida())
                && fachada.registrarJugador(mensaje.getIdJugador());
        String motivo = aceptada ? null : "La partida está llena o no disponible";
        gestorRed.enviarRespuestaUnion(new MensajeRespuestaUnion(
                "HOST", mensaje.getIdMensaje(), aceptada, motivo));
        // El nuevo jugador necesita la instantánea actual para mostrar la sala.
        if (aceptada && alRegistrarJugador != null) {
            alRegistrarJugador.run();
        }
    }

    public void procesarRespuestaUnion(MensajeRespuestaUnion mensaje) {
        if (pantalla != null) {
            if (mensaje.isAceptada()) {
                pantalla.mostrarResultado("Te uniste a la partida correctamente.");
            } else {
                pantalla.mostrarError(mensaje.getMotivo());
            }
        }
        if (notificadorJuego != null) {
            notificadorJuego.mensajeRecibido(mensaje);
        }
        if (!mensaje.isAceptada()) {
            // Una nueva tentativa abrirá otra conexión; libera la rechazada.
            gestorRed.cerrarComunicacion();
        }
    }

    @Override
    public void mensajeRecibido(MensajeRed mensaje) {
        // Este controlador consume los mensajes de unión y delega los del juego.
        if (mensaje instanceof MensajeSolicitudUnion solicitud) {
            procesarSolicitudUnion(solicitud);
        } else if (mensaje instanceof MensajeRespuestaUnion respuesta) {
            procesarRespuestaUnion(respuesta);
        } else if (notificadorJuego != null) {
            notificadorJuego.mensajeRecibido(mensaje);
        }
    }

    @Override
    public void conexionActualizada() {
        if (notificadorJuego != null) {
            notificadorJuego.conexionActualizada();
        }
    }

    @Override
    public void conexionPerdida(String motivo) {
        if (pantalla != null) {
            pantalla.mostrarError("Se perdió la conexión con el Host: " + motivo);
        }
        if (notificadorJuego != null) {
            notificadorJuego.conexionPerdida(motivo);
        }
    }
}
