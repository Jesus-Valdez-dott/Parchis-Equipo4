package Infraestructura;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * A TCP peer connection with serialized message streams.
 */
public final class ConexionPeer {

    private final Socket socket;
    private final ObjectOutputStream salida;
    private final ObjectInputStream entrada;
    private volatile String idPeer;
    private volatile String idJugador;
    private volatile String idJugadorPendiente;
    private volatile boolean activa = true;
    private volatile Consumer<MensajeRed> receptor;
    private volatile Consumer<String> alDesconectar;

    public ConexionPeer(Socket socket) throws IOException {
        this.socket = socket;
        this.idPeer = socket.getRemoteSocketAddress().toString();
        this.salida = new ObjectOutputStream(socket.getOutputStream());
        this.salida.flush();
        this.entrada = new ObjectInputStream(socket.getInputStream());
        this.entrada.setObjectInputFilter(GestorRed::filtrarObjeto);
    }

    public void configurarRecepcion(Consumer<MensajeRed> receptor, Consumer<String> alDesconectar) {
        this.receptor = receptor;
        this.alDesconectar = alDesconectar;
    }

    public void iniciarRecepcion() {
        Thread lector = new Thread(new ManejadorConexion(this), "Parchis-LectorTCP");
        lector.setDaemon(true);
        lector.start();
    }

    public void enviar(MensajeRed mensaje) {
        try {
            synchronized (salida) {
                salida.writeObject(mensaje);
                salida.flush();
                salida.reset();
            }
        } catch (IOException e) {
            notificarDesconexion("No se pudo enviar el mensaje: " + e.getMessage());
            throw new IllegalStateException("Falló el envío por TCP", e);
        }
    }

    Object recibir() throws IOException, ClassNotFoundException {
        Object mensaje = entrada.readObject();
        if (!(mensaje instanceof MensajeRed)) {
            throw new IOException("El peer envió un objeto de red no reconocido");
        }
        return mensaje;
    }

    void entregar(MensajeRed mensaje) {
        Consumer<MensajeRed> destino = receptor;
        if (destino == null) {
            throw new IllegalStateException("No se configuró el receptor de la conexión");
        }
        destino.accept(mensaje);
    }

    void notificarDesconexion(String motivo) {
        if (!activa) {
            return;
        }
        activa = false;
        cerrarSocket();
        Consumer<String> destino = alDesconectar;
        if (destino != null) {
            destino.accept(motivo);
        }
    }

    public void cerrar() {
        if (activa) {
            activa = false;
            cerrarSocket();
        }
    }

    private void cerrarSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            Consumer<String> destino = alDesconectar;
            if (destino != null) {
                destino.accept("No se pudo cerrar la conexión: " + e.getMessage());
            }
        }
    }

    public String getIdPeer() { return idPeer; }
    public void setIdPeer(String idPeer) { this.idPeer = idPeer; }
    public String getIdJugador() { return idJugador; }
    public void setIdJugador(String idJugador) { this.idJugador = idJugador; }
    public String getIdJugadorPendiente() { return idJugadorPendiente; }
    public void setIdJugadorPendiente(String idJugadorPendiente) { this.idJugadorPendiente = idJugadorPendiente; }
    public boolean isActiva() { return activa; }
}
